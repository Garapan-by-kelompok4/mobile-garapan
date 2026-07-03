package com.app.garapan.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.model.User
import com.app.garapan.domain.usecase.EnsureSessionResult
import com.app.garapan.domain.usecase.EnsureSessionUseCase
import com.app.garapan.domain.usecase.LoadSessionUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

sealed interface MainShellEvent {
    data object NavigateToLogin : MainShellEvent
}

data class MainShellUiState(
    val sessionUnavailable: Boolean = false
)

@HiltViewModel
class MainShellViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val ensureSessionUseCase: EnsureSessionUseCase,
    private val loadSessionUseCase: LoadSessionUseCase
) : ViewModel() {
    val currentUser: StateFlow<User?> = observeCurrentUserUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            observeCurrentUserUseCase.snapshot()
        )

    private val _uiState = MutableStateFlow(MainShellUiState())
    val uiState: StateFlow<MainShellUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainShellEvent>()
    val events: SharedFlow<MainShellEvent> = _events.asSharedFlow()

    private val sessionResolveMutex = Mutex()

    fun resolveSessionIfNeeded() {
        viewModelScope.launch {
            sessionResolveMutex.withLock {
                if (currentUser.value != null) return@withLock

                _uiState.update { it.copy(sessionUnavailable = false) }
                when (ensureSessionUseCase()) {
                    EnsureSessionResult.Ready -> Unit
                    EnsureSessionResult.ReadyCached -> refreshSessionSilently()
                    EnsureSessionResult.RequiresLogin ->
                        _events.emit(MainShellEvent.NavigateToLogin)
                    EnsureSessionResult.Unavailable ->
                        _uiState.update { it.copy(sessionUnavailable = true) }
                }
            }
        }
    }

    fun retrySessionResolve() = resolveSessionIfNeeded()

    /**
     * The cached snapshot rendered the shell instantly; fetch the real profile
     * behind it. A definitive auth failure clears tokens and the session, which
     * drops [currentUser] to null and routes back through [resolveSessionIfNeeded];
     * transient failures keep the snapshot working.
     */
    private fun refreshSessionSilently() {
        viewModelScope.launch { loadSessionUseCase() }
    }
}
