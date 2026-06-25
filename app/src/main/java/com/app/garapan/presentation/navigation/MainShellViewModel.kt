package com.app.garapan.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.User
import com.app.garapan.domain.usecase.CheckAuthTokenUseCase
import com.app.garapan.domain.usecase.LoadSessionUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

sealed interface MainShellEvent {
    data object NavigateToLogin : MainShellEvent
}

@HiltViewModel
class MainShellViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val checkAuthTokenUseCase: CheckAuthTokenUseCase,
    private val loadSessionUseCase: LoadSessionUseCase
) : ViewModel() {
    val currentUser: StateFlow<User?> = observeCurrentUserUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            observeCurrentUserUseCase.snapshot()
        )

    private val _events = MutableSharedFlow<MainShellEvent>()
    val events: SharedFlow<MainShellEvent> = _events.asSharedFlow()

    private val sessionResolveMutex = Mutex()

    fun resolveSessionIfNeeded() {
        viewModelScope.launch {
            sessionResolveMutex.withLock {
                if (currentUser.value != null) return@withLock

                if (!checkAuthTokenUseCase()) {
                    _events.emit(MainShellEvent.NavigateToLogin)
                    return@withLock
                }

                when (loadSessionUseCase()) {
                    is Resource.Success -> Unit
                    is Resource.Error -> _events.emit(MainShellEvent.NavigateToLogin)
                    Resource.Loading -> Unit
                }
            }
        }
    }
}
