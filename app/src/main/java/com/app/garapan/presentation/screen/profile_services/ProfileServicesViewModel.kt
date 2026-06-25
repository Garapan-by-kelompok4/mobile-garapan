package com.app.garapan.presentation.screen.profile_services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaStatus
import com.app.garapan.domain.usecase.DeleteJasaUseCase
import com.app.garapan.domain.usecase.GetMyJasaListUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
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

data class ProfileServiceItem(
    val id: String,
    val title: String,
    val budget: String,
    val category: String,
    val deadline: String,
    val teamSize: String,
    val status: String,
    val imageUrl: String = ""
)

data class ProfileServicesUiState(
    val skills: List<String> = emptyList(),
    val services: List<ProfileServiceItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loadErrorMessage: String? = null,
    val isDeleting: Boolean = false
)

sealed interface ProfileServicesEvent {
    data class ShowMessage(val message: String) : ProfileServicesEvent
}

@HiltViewModel
class ProfileServicesViewModel @Inject constructor(
    private val getMyJasaListUseCase: GetMyJasaListUseCase,
    private val deleteJasaUseCase: DeleteJasaUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileServicesUiState(isLoading = true))
    val uiState: StateFlow<ProfileServicesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileServicesEvent>()
    val events: SharedFlow<ProfileServicesEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update { state ->
                    state.copy(skills = user?.mahasiswa?.skills.orEmpty())
                }
            }
        }
        loadMyJasa()
    }

    fun applySavedJasa(jasa: Jasa) {
        val item = toProfileServiceItem(jasa)
        _uiState.update { state ->
            state.copy(
                services = listOf(item) + state.services.filter { it.id != jasa.id },
                isLoading = false,
                loadErrorMessage = null
            )
        }
    }

    fun loadMyJasa(refresh: Boolean = false) {
        viewModelScope.launch {
            val hasCachedServices = _uiState.value.services.isNotEmpty()
            val showFullScreenLoading = !refresh && !hasCachedServices

            _uiState.update {
                it.copy(
                    isLoading = showFullScreenLoading,
                    isRefreshing = refresh && (hasCachedServices || !showFullScreenLoading),
                    loadErrorMessage = if (refresh) null else it.loadErrorMessage
                )
            }

            when (val result = getMyJasaListUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            services = result.data.map(::toProfileServiceItem),
                            loadErrorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    val message = UserMessageLocalizer.localize(result.message)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            services = if (refresh) state.services else emptyList(),
                            loadErrorMessage = if (refresh) null else message
                        )
                    }
                    if (refresh) {
                        _events.emit(ProfileServicesEvent.ShowMessage(message))
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteService(serviceId: String) {
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            when (val result = deleteJasaUseCase(serviceId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            services = it.services.filter { service -> service.id != serviceId }
                        )
                    }
                    loadMyJasa(refresh = true)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    _events.emit(
                        ProfileServicesEvent.ShowMessage(
                            UserMessageLocalizer.localize(result.message)
                        )
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toProfileServiceItem(jasa: Jasa) = ProfileServiceItem(
        id = jasa.id,
        title = jasa.title,
        budget = CurrencyFormatter.formatRupiah(jasa.price),
        category = jasa.kategoriName.ifBlank { "Jasa" },
        deadline = "-",
        teamSize = "-",
        status = when (jasa.status) {
            JasaStatus.ACTIVE -> "Aktif"
            JasaStatus.INACTIVE -> "Nonaktif"
        },
        imageUrl = jasa.imageUrl
    )
}
