package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.usecase.LoginUseCase
import com.app.garapan.domain.usecase.ResendVerificationUseCase
import com.app.garapan.presentation.navigation.Routes
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

enum class LoginTab { STUDENT, CLIENT }

data class LoginUiState(
    val selectedTab: LoginTab = LoginTab.STUDENT,
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
    private val resendVerificationUseCase: ResendVerificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onTabSelected(tab: LoginTab) = _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null, canResendVerification = false) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
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
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(LoginEvent.Navigate(Routes.HOME))
            }
            is LoginResult.RequiresTwoFactor -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        requiresTwoFactor = true,
                        infoMessage = "Two-factor authentication is required. OTP entry is a follow-up for this MVP."
                    )
                }
            }
        }
    }
}
