package com.app.garapan.presentation.screen.project_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.GetProjectDetailUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.domain.usecase.TakeProjectUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class ProjectDetailUiState(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val deadline: String = "",
    val budget: String = "",
    val teamSize: String = "-",
    val clientName: String = "",
    val clientType: String = "",
    val isVerified: Boolean = true,
    val description: String = "",
    val imageUrl: String = "",
    val status: ProjectStatus = ProjectStatus.OPEN,
    val canTake: Boolean = false,
    val showTakeButton: Boolean = false,
    val canEdit: Boolean = false,
    val showEditButton: Boolean = false,
    val isLoading: Boolean = false,
    val isTaking: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ProjectDetailEvent {
    data class ShowMessage(val message: String) : ProjectDetailEvent
    data class NavigateToOrder(val pesananId: String) : ProjectDetailEvent
}

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectDetailUseCase: GetProjectDetailUseCase,
    private val takeProjectUseCase: TakeProjectUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val projectId: String = savedStateHandle["projectId"] ?: ""

    private var currentRole: Role? = null
    private var currentKlienId: String? = null
    private var loadedProject: Project? = null

    private val _uiState = MutableStateFlow(ProjectDetailUiState(isLoading = true))
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProjectDetailEvent>()
    val events: SharedFlow<ProjectDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentRole = user?.role
                currentKlienId = user?.klien?.id
                loadedProject?.let { project ->
                    _uiState.value = project.toUiState()
                }
            }
        }
        loadProjectDetail()
    }

    fun retry() = loadProjectDetail()

    fun onTakeProject() {
        if (_uiState.value.isTaking || !_uiState.value.canTake) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTaking = true) }
            when (val result = takeProjectUseCase(projectId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isTaking = false) }
                    _events.emit(ProjectDetailEvent.NavigateToOrder(result.data.id))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isTaking = false) }
                    _events.emit(
                        ProjectDetailEvent.ShowMessage(
                            UserMessageLocalizer.localize(result.message)
                        )
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadProjectDetail() {
        if (projectId.isBlank()) {
            _uiState.value = ProjectDetailUiState(
                isLoading = false,
                errorMessage = "Proyek tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getProjectDetailUseCase(projectId)) {
                is Resource.Success -> {
                    loadedProject = result.data
                    _uiState.value = result.data.toUiState()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun Project.toUiState(): ProjectDetailUiState {
        val isMahasiswa = currentRole == Role.MAHASISWA
        val isKlienOwner = currentKlienId == klienId && currentRole == Role.KLIEN
        val isOpen = status == ProjectStatus.OPEN && assignedMahasiswaId.isNullOrBlank()

        return ProjectDetailUiState(
            id = id,
            category = kategoriName.ifBlank { "Proyek" },
            title = title,
            deadline = formatDeadline(deadline),
            budget = CurrencyFormatter.formatRupiah(budget),
            clientName = clientName.ifBlank { "Klien" },
            clientType = kategoriName.ifBlank { "Klien" },
            description = description,
            imageUrl = imageUrl,
            status = status,
            canTake = isMahasiswa && isOpen,
            showTakeButton = isMahasiswa && isOpen,
            canEdit = isKlienOwner && isOpen,
            showEditButton = isKlienOwner && isOpen,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun formatDeadline(deadline: String): String {
        if (deadline.isBlank()) return "-"
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            Instant.parse(deadline).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault(deadline.take(10))
    }
}
