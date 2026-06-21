package com.app.garapan.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.usecase.UpdateProfileUseCase
import com.app.garapan.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentSetupState(
    val fullName: String = "",
    val university: String = "",
    val major: String = "",
    val yearsOfExperience: String = "",
    val isYearsDropdownExpanded: Boolean = false,
    val selectedExpertise: Set<String> = emptySet()
)

data class ClientSetupState(
    val fullName: String = "",
    val status: String = "",
    val isStatusDropdownExpanded: Boolean = false,
    val industry: String = "",
    val isIndustryDropdownExpanded: Boolean = false,
    val companyProjectName: String = "",
    val selectedServices: Set<String> = emptySet()
)

data class SetupAccountUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SetupAccountEvent {
    data class Navigate(val route: String) : SetupAccountEvent
}

val studentExpertiseOptions = listOf(
    "UI/UX Design", "Web Development", "Mobile App", "Data Science",
    "Cyber Security", "Cloud Computing", "Backend Dev", "Frontend Dev",
    "Fullstack", "DevOps", "AI/ML", "QA"
)

val clientLookingForOptions = listOf(
    "UI/UX Design", "Backend Dev", "Cloud", "Fullstack",
    "Frontend Dev", "QA", "Mobile Dev", "Data Science",
    "AI/ML", "DevOps", "Cyber Security"
)

val statusOptions = listOf("Individual", "Company", "Startup", "Government")
val industryOptions = listOf("Technology", "Finance", "Education", "Healthcare", "Retail", "Other")
val yearsOfExperienceOptions = listOf("0-1 years", "1-3 years", "3-5 years", "5+ years")

@HiltViewModel
class SetupAccountViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _student = MutableStateFlow(StudentSetupState())
    val student: StateFlow<StudentSetupState> = _student.asStateFlow()

    private val _client = MutableStateFlow(ClientSetupState())
    val client: StateFlow<ClientSetupState> = _client.asStateFlow()

    private val _uiState = MutableStateFlow(SetupAccountUiState())
    val uiState: StateFlow<SetupAccountUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SetupAccountEvent>()
    val events: SharedFlow<SetupAccountEvent> = _events.asSharedFlow()

    // Student
    fun onStudentFullNameChanged(v: String) = _student.update { it.copy(fullName = v) }
    fun onUniversityChanged(v: String) = _student.update { it.copy(university = v) }
    fun onMajorChanged(v: String) = _student.update { it.copy(major = v) }
    fun onYearsSelected(v: String) = _student.update { it.copy(yearsOfExperience = v, isYearsDropdownExpanded = false) }
    fun onYearsDropdownToggle() = _student.update { it.copy(isYearsDropdownExpanded = !it.isYearsDropdownExpanded) }
    fun onToggleStudentExpertise(tag: String) = _student.update {
        val updated = if (tag in it.selectedExpertise) it.selectedExpertise - tag else it.selectedExpertise + tag
        it.copy(selectedExpertise = updated)
    }

    // Client
    fun onClientFullNameChanged(v: String) = _client.update { it.copy(fullName = v) }
    fun onStatusSelected(v: String) = _client.update { it.copy(status = v, isStatusDropdownExpanded = false) }
    fun onStatusDropdownToggle() = _client.update { it.copy(isStatusDropdownExpanded = !it.isStatusDropdownExpanded) }
    fun onIndustrySelected(v: String) = _client.update { it.copy(industry = v, isIndustryDropdownExpanded = false) }
    fun onIndustryDropdownToggle() = _client.update { it.copy(isIndustryDropdownExpanded = !it.isIndustryDropdownExpanded) }
    fun onCompanyProjectNameChanged(v: String) = _client.update { it.copy(companyProjectName = v) }
    fun onToggleClientService(tag: String) = _client.update {
        val updated = if (tag in it.selectedServices) it.selectedServices - tag else it.selectedServices + tag
        it.copy(selectedServices = updated)
    }

    fun onComplete(role: String) {
        viewModelScope.launch {
            _uiState.value = SetupAccountUiState(isLoading = true)
            val params = if (role == "student") {
                val state = _student.value
                UpdateProfileParams(
                    university = state.university.takeIf { it.isNotBlank() },
                    skills = state.selectedExpertise.toList(),
                    bio = buildStudentBio(state).takeIf { it.isNotBlank() }
                )
            } else {
                val state = _client.value
                UpdateProfileParams(
                    companyName = state.companyProjectName.takeIf { it.isNotBlank() },
                    bio = buildClientBio(state).takeIf { it.isNotBlank() }
                )
            }

            when (val result = updateProfileUseCase(params)) {
                is Resource.Success -> {
                    _uiState.value = SetupAccountUiState(isLoading = false)
                    _events.emit(SetupAccountEvent.Navigate(Routes.HOME))
                }
                is Resource.Error -> _uiState.value = SetupAccountUiState(
                    isLoading = false,
                    errorMessage = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }

    private fun buildStudentBio(state: StudentSetupState): String =
        listOf(state.fullName, state.major, state.yearsOfExperience)
            .filter { it.isNotBlank() }
            .joinToString(separator = " | ")

    private fun buildClientBio(state: ClientSetupState): String =
        listOf(state.fullName, state.status, state.industry)
            .filter { it.isNotBlank() }
            .joinToString(separator = " | ")
}
