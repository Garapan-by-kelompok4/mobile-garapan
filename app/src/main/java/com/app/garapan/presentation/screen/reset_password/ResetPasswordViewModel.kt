package com.app.garapan.presentation.screen.reset_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.ResetPasswordUseCase
import com.app.garapan.domain.validation.PasswordValidator
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

data class ResetPasswordUiState(
    val email: String = "",
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

sealed interface ResetPasswordEvent {
    data class Navigate(val route: String) : ResetPasswordEvent
    data class Toast(val message: String) : ResetPasswordEvent
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResetPasswordEvent>()
    val events: SharedFlow<ResetPasswordEvent> = _events.asSharedFlow()

    fun setEmail(email: String) = _uiState.update {
        it.copy(email = email, errorMessage = null)
    }

    fun onTokenChanged(value: String) = _uiState.update {
        it.copy(token = value, errorMessage = null)
    }

    fun onNewPasswordChanged(value: String) = _uiState.update {
        it.copy(newPassword = value, errorMessage = null)
    }

    fun onConfirmPasswordChanged(value: String) = _uiState.update {
        it.copy(confirmPassword = value, errorMessage = null)
    }

    fun onTogglePasswordVisibility() = _uiState.update {
        it.copy(isPasswordVisible = !it.isPasswordVisible)
    }

    fun onToggleConfirmPasswordVisibility() = _uiState.update {
        it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
    }

    fun onSubmit() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val result = resetPasswordUseCase(state.token.trim(), state.newPassword)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(ResetPasswordEvent.Toast("Password updated. Please sign in."))
                    _events.emit(ResetPasswordEvent.Navigate(Routes.LOGIN))
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun validate(state: ResetPasswordUiState): String? =
        when {
            state.token.isBlank() -> "Reset token is required."
            state.newPassword != state.confirmPassword -> "Passwords do not match."
            !PasswordValidator.isValid(state.newPassword) ->
                "Password must be at least 8 characters and include lowercase, uppercase, number, and symbol."
            else -> null
        }
}
