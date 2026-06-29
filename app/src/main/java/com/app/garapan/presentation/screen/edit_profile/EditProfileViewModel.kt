package com.app.garapan.presentation.screen.edit_profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.data.util.PortfolioImageReader
import com.app.garapan.data.util.PortfolioImageReadResult
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ProfileStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.UpdateProfileUseCase
import com.app.garapan.domain.usecase.UploadAvatarUseCase
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class EditProfileUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val status: ProfileStatus? = null,
    val isStatusDropdownExpanded: Boolean = false,
    val organization: String = "",
    val linkedinUrl: String = "",
    val role: Role? = null,
    val avatarUrl: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false
) {
    val isKlien: Boolean get() = role == Role.KLIEN
    val initials: String
        get() = fullName.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
}

val profileStatusOptions = ProfileStatus.entries

sealed interface EditProfileEvent {
    data class ShowMessage(val message: String) : EditProfileEvent
    data object Saved : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditProfileEvent>()
    val events: SharedFlow<EditProfileEvent> = _events.asSharedFlow()

    private var prefilled = false

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                if (user == null) return@collect
                // Prefill once from the cached session user; afterwards only refresh
                // avatar so we don't clobber the user's in-progress edits.
                _uiState.update { state ->
                    if (!prefilled) {
                        prefilled = true
                        state.copy(
                            fullName = user.resolveName(),
                            phoneNumber = user.phoneNumber.orEmpty(),
                            status = user.status,
                            organization = user.klien?.companyName.orEmpty(),
                            linkedinUrl = user.socialAccounts.linkedinUrl.orEmpty(),
                            role = user.role,
                            avatarUrl = user.avatarUrl,
                            isLoading = false
                        )
                    } else {
                        state.copy(role = user.role, avatarUrl = user.avatarUrl)
                    }
                }
            }
        }
    }

    fun onFullNameChanged(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onPhoneNumberChanged(value: String) =
        _uiState.update { it.copy(phoneNumber = value.filter { c -> c.isDigit() || c == '+' }.take(20)) }

    fun onStatusSelected(value: ProfileStatus) =
        _uiState.update { it.copy(status = value, isStatusDropdownExpanded = false) }

    fun onStatusDropdownToggle() =
        _uiState.update { it.copy(isStatusDropdownExpanded = !it.isStatusDropdownExpanded) }

    fun onStatusDropdownDismiss() =
        _uiState.update { it.copy(isStatusDropdownExpanded = false) }

    fun onOrganizationChanged(value: String) = _uiState.update { it.copy(organization = value) }
    fun onLinkedinUrlChanged(value: String) = _uiState.update { it.copy(linkedinUrl = value) }

    fun onAvatarSelected(uri: Uri, readContext: Context = context) {
        if (_uiState.value.isUploadingAvatar) return
        _uiState.update { it.copy(isUploadingAvatar = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                PortfolioImageReader.readCompressedWithResult(readContext, uri)
            }
            when (result) {
                is PortfolioImageReadResult.Success -> {
                    if (result.image.bytes.size > PortfolioImageReader.MAX_BYTES) {
                        _uiState.update { it.copy(isUploadingAvatar = false) }
                        _events.emit(EditProfileEvent.ShowMessage("Ukuran gambar maksimal 5 MB."))
                        return@launch
                    }
                    when (val upload = uploadAvatarUseCase(result.image)) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(isUploadingAvatar = false, avatarUrl = upload.data.avatarUrl)
                            }
                            _events.emit(EditProfileEvent.ShowMessage("Foto profil diperbarui."))
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(isUploadingAvatar = false) }
                            _events.emit(
                                EditProfileEvent.ShowMessage(UserMessageLocalizer.localize(upload.message))
                            )
                        }
                        Resource.Loading -> _uiState.update { it.copy(isUploadingAvatar = false) }
                    }
                }
                is PortfolioImageReadResult.Failure -> {
                    _uiState.update { it.copy(isUploadingAvatar = false) }
                    _events.emit(
                        EditProfileEvent.ShowMessage("Gagal memproses gambar. Coba pilih gambar lain.")
                    )
                }
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading) return

        val params = UpdateProfileParams(
            displayName = state.fullName.trim().ifBlank { null },
            phoneNumber = state.phoneNumber.trim().ifBlank { null },
            status = state.status,
            linkedinUrl = state.linkedinUrl.trim().ifBlank { null },
            // companyName is Klien-only on the backend; never send it for a Mahasiswa.
            companyName = state.organization.trim().takeIf { state.isKlien && it.isNotBlank() }
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = updateProfileUseCase(params)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(EditProfileEvent.Saved)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(
                        EditProfileEvent.ShowMessage(UserMessageLocalizer.localize(result.message))
                    )
                }
                Resource.Loading -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

private fun User.resolveName(): String =
    displayName?.takeIf { it.isNotBlank() }
        ?: mahasiswa?.fullName?.takeIf { it.isNotBlank() }
        ?: ""
