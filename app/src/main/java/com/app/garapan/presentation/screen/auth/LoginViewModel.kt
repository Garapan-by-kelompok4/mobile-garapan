package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.usecase.GetMeUseCase
import com.app.garapan.domain.usecase.LoginUseCase
import com.app.garapan.domain.usecase.ResendVerificationUseCase
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.navigation.authDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val canResendVerification: Boolean = false,
    val requiresTwoFactor: Boolean = false
)

sealed interface LoginEvent {
    data class Navigate(val route: String) : LoginEvent
    data class Toast(val message: String) : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getMeUseCase: GetMeUseCase,
    private val resendVerificationUseCase: ResendVerificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onEmailChanged(value: String) = _uiState.update {
        it.copy(
            email = value,
            errorMessage = null,
            canResendVerification = false,
            requiresTwoFactor = false
        )
    }
    fun onPasswordChanged(value: String) = _uiState.update {
        it.copy(password = value, errorMessage = null, requiresTwoFactor = false)
    }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onSignIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    infoMessage = null,
                    canResendVerification = false,
                    requiresTwoFactor = false
                )
            }
            when (val result = loginUseCase(state.email.trim(), state.password)) {
                is Resource.Success -> handleLoginSuccess(result.data)
                is Resource.Error -> {
                    val unverified = result.message.contains("not verified", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            canResendVerification = unverified
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onResendVerification() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your email address first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val result = resendVerificationUseCase(email)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Verification email sent. Check your inbox."
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun handleLoginSuccess(result: LoginResult) {
        when (result) {
            is LoginResult.Authenticated -> {
                routeAfterAuthenticatedLogin()
            }
            is LoginResult.RequiresTwoFactor -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        requiresTwoFactor = true,
                        infoMessage = null,
                        errorMessage = null
                    )
                }
                _events.emit(LoginEvent.Navigate(Routes.twoFactorRoute(result.preAuthToken)))
            }
        }
    }

    private suspend fun routeAfterAuthenticatedLogin() {
        when (val result = getMeUseCase()) {
            is Resource.Success -> {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(LoginEvent.Navigate(result.data.authDestination()))
            }
            is Resource.Error -> _uiState.update {
                it.copy(isLoading = false, errorMessage = result.message)
            }
            Resource.Loading -> Unit
        }
    }
}
