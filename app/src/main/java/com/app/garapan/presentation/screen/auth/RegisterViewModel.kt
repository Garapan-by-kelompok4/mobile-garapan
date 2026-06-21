package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.GetMeUseCase
import com.app.garapan.domain.usecase.GoogleSignInUseCase
import com.app.garapan.domain.usecase.RegisterUseCase
import com.app.garapan.domain.usecase.ResendVerificationUseCase
import com.app.garapan.domain.validation.PasswordValidator
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

enum class LoginTab { STUDENT, CLIENT }

data class RegisterUiState(
    val selectedTab: LoginTab = LoginTab.STUDENT,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isAwaitingEmailVerification: Boolean = false
)

sealed interface RegisterEvent {
    data class Navigate(val route: String) : RegisterEvent
    data class Toast(val message: String) : RegisterEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val getMeUseCase: GetMeUseCase,
    private val resendVerificationUseCase: ResendVerificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>()
    val events: SharedFlow<RegisterEvent> = _events.asSharedFlow()

    fun onTabSelected(tab: LoginTab) = _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun onToggleConfirmPasswordVisibility() = _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

    fun onGoogleSignIn(idToken: String, role: Role) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val result = googleSignInUseCase(idToken, role)) {
                is Resource.Success -> routeAfterAuthenticatedGoogleSignIn()
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun onSignUp() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val role = if (state.selectedTab == LoginTab.STUDENT) Role.MAHASISWA else Role.KLIEN
            when (val result = registerUseCase(state.email.trim(), state.password, role)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(RegisterEvent.Navigate(Routes.verifyEmailRoute(state.email.trim())))
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
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
                    it.copy(isLoading = false, infoMessage = "Verification email sent. Check your inbox.")
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun validate(state: RegisterUiState): String? =
        when {
            state.email.isBlank() -> "Email address is required."
            state.password != state.confirmPassword -> "Passwords do not match."
            !PasswordValidator.isValid(state.password) ->
                "Password must be at least 8 characters and include lowercase, uppercase, number, and symbol."
            else -> null
        }

    private suspend fun routeAfterAuthenticatedGoogleSignIn() {
        when (val result = getMeUseCase()) {
            is Resource.Success -> {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(RegisterEvent.Navigate(result.data.authDestination()))
            }
            is Resource.Error -> _uiState.update {
                it.copy(isLoading = false, errorMessage = result.message)
            }
            Resource.Loading -> Unit
        }
    }
}
