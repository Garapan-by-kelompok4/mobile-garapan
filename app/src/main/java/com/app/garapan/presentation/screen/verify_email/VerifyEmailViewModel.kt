package com.app.garapan.presentation.screen.verify_email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.ResendVerificationUseCase
import com.app.garapan.domain.usecase.VerifyEmailUseCase
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

data class VerifyEmailUiState(
    val email: String = "",
    val token: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

sealed interface VerifyEmailEvent {
    data class Navigate(val route: String) : VerifyEmailEvent
    data class Toast(val message: String) : VerifyEmailEvent
}

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val resendVerificationUseCase: ResendVerificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyEmailUiState())
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VerifyEmailEvent>()
    val events: SharedFlow<VerifyEmailEvent> = _events.asSharedFlow()

    fun setEmail(email: String) = _uiState.update {
        it.copy(email = email, errorMessage = null)
    }

    fun onTokenChanged(value: String) = _uiState.update {
        it.copy(token = value, errorMessage = null)
    }

    fun onVerify() {
        val token = _uiState.value.token.trim()
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Verification token is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val result = verifyEmailUseCase(token)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(VerifyEmailEvent.Toast("Email verified. Please sign in."))
                    _events.emit(VerifyEmailEvent.Navigate(Routes.LOGIN))
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onResend() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email address is required.") }
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
}
