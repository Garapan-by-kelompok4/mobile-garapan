package com.app.garapan.presentation.screen.change_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.ChangePasswordUseCase
import com.app.garapan.domain.usecase.LogoutUseCase
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isCurrentVisible: Boolean = false,
    val isNewVisible: Boolean = false,
    val isConfirmVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ChangePasswordEvent {
    data class ShowMessage(val message: String) : ChangePasswordEvent
    data object Success : ChangePasswordEvent
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChangePasswordEvent>()
    val events: SharedFlow<ChangePasswordEvent> = _events.asSharedFlow()

    fun onCurrentPasswordChanged(value: String) =
        _uiState.update { it.copy(currentPassword = value, errorMessage = null) }

    fun onNewPasswordChanged(value: String) =
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }

    fun onConfirmPasswordChanged(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun onToggleCurrentVisibility() =
        _uiState.update { it.copy(isCurrentVisible = !it.isCurrentVisible) }

    fun onToggleNewVisibility() =
        _uiState.update { it.copy(isNewVisible = !it.isNewVisible) }

    fun onToggleConfirmVisibility() =
        _uiState.update { it.copy(isConfirmVisible = !it.isConfirmVisible) }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = changePasswordUseCase(state.currentPassword, state.newPassword)) {
                is Resource.Success -> {
                    // Changing the password revokes all sessions on the backend, so sign out
                    // locally and route the user back to login.
                    logoutUseCase()
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(ChangePasswordEvent.Success)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(
                        ChangePasswordEvent.ShowMessage(UserMessageLocalizer.localize(result.message))
                    )
                }
                Resource.Loading -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun validate(state: ChangePasswordUiState): String? = when {
        state.currentPassword.isBlank() -> "Masukkan kata sandi saat ini."
        state.newPassword.isBlank() -> "Masukkan kata sandi baru."
        !isStrongPassword(state.newPassword) ->
            "Kata sandi baru minimal 8 karakter dan memuat huruf besar, huruf kecil, angka, dan simbol."
        state.newPassword != state.confirmPassword -> "Konfirmasi kata sandi tidak cocok."
        state.newPassword == state.currentPassword -> "Kata sandi baru harus berbeda dari yang lama."
        else -> null
    }

    private fun isStrongPassword(password: String): Boolean =
        password.length >= 8 &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { it.isDigit() } &&
            password.any { !it.isLetterOrDigit() }
}
