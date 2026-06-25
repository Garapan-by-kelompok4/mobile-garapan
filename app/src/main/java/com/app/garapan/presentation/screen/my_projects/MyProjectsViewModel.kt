package com.app.garapan.presentation.screen.my_projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.DeleteProjectUseCase
import com.app.garapan.domain.usecase.GetMyProjectsUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
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

data class MyProjectItem(
    val id: String,
    val title: String,
    val budget: String,
    val category: String,
    val deadline: String,
    val status: String,
    val assigneeName: String = ""
)

data class MyProjectsUiState(
    val screenTitle: String = "Proyek Saya",
    val projects: List<MyProjectItem> = emptyList(),
    val canDelete: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isDeleting: Boolean = false,
    val loadErrorMessage: String? = null
)

sealed interface MyProjectsEvent {
    data class ShowMessage(val message: String) : MyProjectsEvent
}

@HiltViewModel
class MyProjectsViewModel @Inject constructor(
    private val getMyProjectsUseCase: GetMyProjectsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private var currentRole: Role? = null

    private val _uiState = MutableStateFlow(MyProjectsUiState(isLoading = true))
    val uiState: StateFlow<MyProjectsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MyProjectsEvent>()
    val events: SharedFlow<MyProjectsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentRole = user?.role
                _uiState.update {
                    it.copy(
                        screenTitle = when (user?.role) {
                            Role.MAHASISWA -> "Proyek Diambil"
                            else -> "Proyek Saya"
                        },
                        canDelete = user?.role == Role.KLIEN || user?.role == Role.ADMIN
                    )
                }
            }
        }
        loadProjects()
    }

    fun loadProjects(refresh: Boolean = false) {
        viewModelScope.launch {
            val hasCachedProjects = _uiState.value.projects.isNotEmpty()
            val showFullScreenLoading = !refresh && !hasCachedProjects

            _uiState.update {
                it.copy(
                    isLoading = showFullScreenLoading,
                    isRefreshing = refresh && (hasCachedProjects || !showFullScreenLoading),
                    loadErrorMessage = if (refresh) null else it.loadErrorMessage
                )
            }

            when (val result = getMyProjectsUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            projects = result.data.map(::toMyProjectItem),
                            loadErrorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    val message = UserMessageLocalizer.localize(result.message)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            projects = if (refresh) state.projects else emptyList(),
                            loadErrorMessage = if (refresh) null else message
                        )
                    }
                    if (refresh) {
                        _events.emit(MyProjectsEvent.ShowMessage(message))
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteProject(projectId: String) {
        if (_uiState.value.isDeleting || !_uiState.value.canDelete) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            when (val result = deleteProjectUseCase(projectId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            projects = it.projects.filter { project -> project.id != projectId }
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    _events.emit(
                        MyProjectsEvent.ShowMessage(
                            UserMessageLocalizer.localize(result.message)
                        )
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toMyProjectItem(project: Project) = MyProjectItem(
        id = project.id,
        title = project.title,
        budget = CurrencyFormatter.formatRupiah(project.budget),
        category = project.kategoriName.ifBlank { "Proyek" },
        deadline = formatDeadline(project.deadline),
        status = when (project.status) {
            ProjectStatus.OPEN -> "Terbuka"
            ProjectStatus.CLOSED -> "Ditutup"
        },
        assigneeName = project.assigneeName
    )

    private fun formatDeadline(deadline: String): String {
        if (deadline.isBlank()) return "-"
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
            Instant.parse(deadline).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault(deadline.take(10))
    }
}
