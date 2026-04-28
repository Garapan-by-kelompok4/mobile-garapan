package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class LoginTab { STUDENT, CLIENT }

data class LoginUiState(
    val selectedTab: LoginTab = LoginTab.STUDENT,
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onTabSelected(tab: LoginTab) = _uiState.update { it.copy(selectedTab = tab) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
}
