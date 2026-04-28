package com.app.garapan.presentation.screen.edit_profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class EditProfileUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val status: String = "",
    val organization: String = "",
    val socialAccount: String = ""
)

@HiltViewModel
class EditProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun onFullNameChanged(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onPhoneNumberChanged(value: String) = _uiState.update { it.copy(phoneNumber = value) }
    fun onStatusChanged(value: String) = _uiState.update { it.copy(status = value) }
    fun onOrganizationChanged(value: String) = _uiState.update { it.copy(organization = value) }
    fun onSocialAccountChanged(value: String) = _uiState.update { it.copy(socialAccount = value) }
}
