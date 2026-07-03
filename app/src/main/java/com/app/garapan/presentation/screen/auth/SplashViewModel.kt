package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.usecase.CheckAuthTokenUseCase
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.notification.FcmTokenRegistrar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val isLoading: Boolean = true
)

sealed interface SplashEvent {
    data class Navigate(val route: String) : SplashEvent
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkAuthTokenUseCase: CheckAuthTokenUseCase,
    private val fcmTokenRegistrar: FcmTokenRegistrar
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SplashEvent>(replay = 1)
    val events: SharedFlow<SplashEvent> = _events.asSharedFlow()

    init {
        checkAuth()
    }

    // Only the local token check gates the splash; the session itself is
    // resolved by MainShell (cached snapshot first, network refresh behind it),
    // so cold start never waits on a network round-trip.
    private fun checkAuth() {
        viewModelScope.launch {
            val isLoggedIn = checkAuthTokenUseCase()
            _uiState.value = SplashUiState(isLoading = false)
            if (!isLoggedIn) {
                _events.emit(SplashEvent.Navigate(Routes.LOGIN))
                return@launch
            }
            fcmTokenRegistrar.registerCurrentToken(viewModelScope)
            _events.emit(SplashEvent.Navigate(Routes.MAIN))
        }
    }
}
