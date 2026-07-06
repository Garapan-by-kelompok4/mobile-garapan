package com.app.garapan.presentation.screen.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.ForgotPasswordUseCase
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

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val canEnterResetToken: Boolean = false
)

sealed interface ForgotPasswordEvent {
    data class Navigate(val route: String) : ForgotPasswordEvent
    data class Toast(val message: String) : ForgotPasswordEvent
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    fun onEmailChanged(value: String) = _uiState.update {
        it.copy(
            email = value,
            errorMessage = null,
            infoMessage = null,
            canEnterResetToken = false
        )
    }

    fun onSubmit() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email address is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val result = forgotPasswordUseCase(email)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "If that email is registered, a reset token has been sent.",
                            canEnterResetToken = true
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        canEnterResetToken = false
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onEnterResetToken() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email address is required.") }
            return
        }

        viewModelScope.launch {
            _events.emit(ForgotPasswordEvent.Navigate(Routes.resetPasswordRoute(email)))
        }
    }
}
