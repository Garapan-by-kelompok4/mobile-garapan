package com.app.garapan.presentation.screen.two_factor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.LoadSessionUseCase
import com.app.garapan.domain.usecase.ResendOtpUseCase
import com.app.garapan.domain.usecase.VerifyOtpUseCase
import com.app.garapan.presentation.navigation.authDestination
import com.app.garapan.presentation.notification.FcmTokenRegistrar
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

data class TwoFactorUiState(
    val preAuthToken: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

sealed interface TwoFactorEvent {
    data class Navigate(val route: String) : TwoFactorEvent
    data class Toast(val message: String) : TwoFactorEvent
}

@HiltViewModel
class TwoFactorViewModel @Inject constructor(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resendOtpUseCase: ResendOtpUseCase,
    private val loadSessionUseCase: LoadSessionUseCase,
    private val fcmTokenRegistrar: FcmTokenRegistrar
) : ViewModel() {

    private val _uiState = MutableStateFlow(TwoFactorUiState())
    val uiState: StateFlow<TwoFactorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TwoFactorEvent>()
    val events: SharedFlow<TwoFactorEvent> = _events.asSharedFlow()

    fun setPreAuthToken(preAuthToken: String) = _uiState.update {
        it.copy(preAuthToken = preAuthToken, errorMessage = null)
    }

    fun onOtpChanged(value: String) = _uiState.update {
        it.copy(
            otp = value.filter(Char::isDigit).take(6),
            errorMessage = null,
            infoMessage = null
        )
    }

    fun onVerify() {
        val state = _uiState.value
        val otp = state.otp.trim()
        when {
            state.preAuthToken.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Two-factor session expired. Please sign in again.") }
                return
            }
            otp.length != 6 -> {
                _uiState.update { it.copy(errorMessage = "Enter the 6-digit code.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val result = verifyOtpUseCase(state.preAuthToken, otp)) {
                is Resource.Success -> routeAfterVerification()
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onResend() {
        val preAuthToken = _uiState.value.preAuthToken
        if (preAuthToken.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Two-factor session expired. Please sign in again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, errorMessage = null, infoMessage = null) }
            when (val result = resendOtpUseCase(preAuthToken)) {
                is Resource.Success -> {
                    val message = "A new code has been sent."
                    _uiState.update { it.copy(isResending = false, infoMessage = message) }
                    _events.emit(TwoFactorEvent.Toast(message))
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isResending = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun routeAfterVerification() {
        when (val result = loadSessionUseCase()) {
            is Resource.Success -> {
                fcmTokenRegistrar.registerCurrentToken()
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(TwoFactorEvent.Navigate(result.data.authDestination()))
            }
            is Resource.Error -> _uiState.update {
                it.copy(isLoading = false, errorMessage = result.message)
            }
            Resource.Loading -> Unit
        }
    }
}
