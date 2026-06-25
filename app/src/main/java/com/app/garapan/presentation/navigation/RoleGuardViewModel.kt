package com.app.garapan.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.model.User
import com.app.garapan.domain.usecase.EnsureSessionResult
import com.app.garapan.domain.usecase.EnsureSessionUseCase
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

sealed interface RoleGuardEvent {
    data object NavigateToLogin : RoleGuardEvent
}

@HiltViewModel
class RoleGuardViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val ensureSessionUseCase: EnsureSessionUseCase
) : ViewModel() {
    val currentUser: StateFlow<User?> = observeCurrentUserUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            observeCurrentUserUseCase.snapshot()
        )

    private val _events = MutableSharedFlow<RoleGuardEvent>()
    val events: SharedFlow<RoleGuardEvent> = _events.asSharedFlow()

    private val sessionResolveMutex = Mutex()

    fun resolveSessionIfNeeded() {
        viewModelScope.launch {
            sessionResolveMutex.withLock {
                if (currentUser.value != null) return@withLock

                if (ensureSessionUseCase() == EnsureSessionResult.RequiresLogin) {
                    _events.emit(RoleGuardEvent.NavigateToLogin)
                }
            }
        }
    }
}
