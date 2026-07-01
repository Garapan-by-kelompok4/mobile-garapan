package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.CheckAuthTokenUseCase
import com.app.garapan.domain.usecase.LoadSessionUseCase
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.navigation.authDestination
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
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface SplashEvent {
    data class Navigate(val route: String) : SplashEvent
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkAuthTokenUseCase: CheckAuthTokenUseCase,
    private val loadSessionUseCase: LoadSessionUseCase,
    private val fcmTokenRegistrar: FcmTokenRegistrar
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SplashEvent>(replay = 1)
    val events: SharedFlow<SplashEvent> = _events.asSharedFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            val isLoggedIn = checkAuthTokenUseCase()
            if (!isLoggedIn) {
                _uiState.value = SplashUiState(isLoading = false)
                _events.emit(SplashEvent.Navigate(Routes.LOGIN))
                return@launch
            }

            fcmTokenRegistrar.registerCurrentToken(viewModelScope)
            when (val result = loadSessionUseCase()) {
                is Resource.Success -> {
                    _uiState.value = SplashUiState(isLoading = false)
                    _events.emit(SplashEvent.Navigate(result.data.authDestination()))
                }
                is Resource.Error -> {
                    _uiState.value = SplashUiState(isLoading = false, errorMessage = result.message)
                    _events.emit(SplashEvent.Navigate(Routes.LOGIN))
                }
                Resource.Loading -> Unit
            }
        }
    }

}
