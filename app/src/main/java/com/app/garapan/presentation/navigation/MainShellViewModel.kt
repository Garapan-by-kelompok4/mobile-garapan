package com.app.garapan.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.model.User
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainShellViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {
    val currentUser: StateFlow<User?> = observeCurrentUserUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            observeCurrentUserUseCase.snapshot()
        )
}
