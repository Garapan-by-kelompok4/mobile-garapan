package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class RegisterUiState(
    val selectedTab: LoginTab = LoginTab.STUDENT,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onTabSelected(tab: LoginTab) = _uiState.update { it.copy(selectedTab = tab) }
    fun onFullNameChanged(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value) }
    fun onConfirmPasswordChanged(value: String) = _uiState.update { it.copy(confirmPassword = value) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun onToggleConfirmPasswordVisibility() = _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
}
