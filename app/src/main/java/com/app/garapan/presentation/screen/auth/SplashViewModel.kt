package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.User
import com.app.garapan.domain.usecase.CheckAuthTokenUseCase
import com.app.garapan.domain.usecase.GetMeUseCase
import com.app.garapan.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val getMeUseCase: GetMeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SplashEvent>()
    val events: SharedFlow<SplashEvent> = _events.asSharedFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            delay(2000L)
            val isLoggedIn = checkAuthTokenUseCase()
            if (!isLoggedIn) {
                _uiState.value = SplashUiState(isLoading = false)
                _events.emit(SplashEvent.Navigate(Routes.LOGIN))
                return@launch
            }

            when (val result = getMeUseCase()) {
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

    private fun User.authDestination(): String =
        if (isProfileIncomplete()) Routes.setupRoute(role.setupRouteParam()) else Routes.HOME

    private fun User.isProfileIncomplete(): Boolean =
        when (role) {
            Role.MAHASISWA -> mahasiswa == null || mahasiswa.university.isBlank() || mahasiswa.bio.isBlank()
            Role.KLIEN -> klien == null || klien.bio.isBlank()
            Role.ADMIN -> false
        }

    private fun Role.setupRouteParam(): String =
        when (this) {
            Role.MAHASISWA -> "student"
            Role.KLIEN -> "client"
            Role.ADMIN -> "admin"
        }
}
