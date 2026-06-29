package com.app.garapan.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.User
import com.app.garapan.domain.usecase.LogoutUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.presentation.navigation.Routes
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

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val role: Role? = null,
    val avatarUrl: String? = null
)

sealed interface ProfileEvent {
    data class Navigate(val route: String) : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        name = user.resolveDisplayName().ifBlank { it.name },
                        email = user?.email.orEmpty().ifBlank { it.email },
                        role = user?.role,
                        avatarUrl = user?.avatarUrl
                    )
                }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase()
            _events.emit(ProfileEvent.Navigate(Routes.LOGIN))
        }
    }
}

private fun User?.resolveDisplayName(): String {
    if (this == null) return ""
    return displayName?.takeIf { it.isNotBlank() }
        ?: mahasiswa?.fullName?.takeIf { it.isNotBlank() }
        ?: klien?.companyName?.takeIf { it.isNotBlank() }
        ?: mahasiswa?.university?.takeIf { it.isNotBlank() }
        ?: email.substringBefore("@")
}
