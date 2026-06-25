package com.app.garapan.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Artikel
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.model.TopWorker
import com.app.garapan.domain.usecase.GetArtikelListUseCase
import com.app.garapan.domain.usecase.GetJasaListUseCase
import com.app.garapan.domain.usecase.GetProjectListUseCase
import com.app.garapan.domain.usecase.GetTopWorkersUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class ProjectItem(
    val id: String,
    val title: String,
    val budget: String,
    val category: String,
    val deadline: String,
    val clientName: String,
    val imageUrl: String = ""
)

data class ServiceItem(
    val id: String,
    val title: String,
    val price: String,
    val category: String,
    val workerName: String,
    val rating: Float,
    val imageUrl: String = ""
)

data class TopWorkerItem(
    val id: String,
    val userId: String,
    val name: String,
    val skill: String,
    val rating: Float,
    val projectsDone: Int,
    val avatarUrl: String? = null
)

data class BlogItem(
    val id: String,
    val title: String,
    val category: String,
    val readTime: String,
    val date: String = ""
)

data class HomeUiState(
    val selectedNavIndex: Int = 0,
    val projects: List<ProjectItem> = emptyList(),
    val isProjectsLoading: Boolean = false,
    val projectsError: String? = null,
    val services: List<ServiceItem> = emptyList(),
    val isServicesLoading: Boolean = false,
    val servicesError: String? = null,
    val topWorkers: List<TopWorkerItem> = emptyList(),
    val isTopWorkersLoading: Boolean = false,
    val topWorkersError: String? = null,
    val blogs: List<BlogItem> = emptyList(),
    val isBlogsLoading: Boolean = false,
    val blogsError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopWorkersUseCase: GetTopWorkersUseCase,
    private val getArtikelListUseCase: GetArtikelListUseCase,
    private val getJasaListUseCase: GetJasaListUseCase,
    private val getProjectListUseCase: GetProjectListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFeaturedProjects()
        loadTopWorkers()
        loadBlogs()
        loadFeaturedServices()
    }

    fun onNavItemSelected(index: Int) = _uiState.value.let {
        _uiState.value = it.copy(selectedNavIndex = index)
    }

    fun retryTopWorkers() = loadTopWorkers()

    fun retryBlogs() = loadBlogs()

    fun retryServices() = loadFeaturedServices()

    fun retryProjects() = loadFeaturedProjects()

    fun refreshProjects() = loadFeaturedProjects()

    private fun loadFeaturedProjects(refresh: Boolean = false) {
        viewModelScope.launch {
            val hasCachedProjects = _uiState.value.projects.isNotEmpty()
            val showFullScreenLoading = !refresh && !hasCachedProjects

            _uiState.update {
                it.copy(
                    isProjectsLoading = showFullScreenLoading,
                    projectsError = if (refresh) null else it.projectsError
                )
            }

            when (val result = getProjectListUseCase(ProjectListFilters(limit = 4))) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            projects = result.data.map(::toProjectItem),
                            isProjectsLoading = false,
                            projectsError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            projects = emptyList(),
                            isProjectsLoading = false,
                            projectsError = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadFeaturedServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isServicesLoading = true, servicesError = null) }
            when (val result = getJasaListUseCase(JasaListFilters(limit = 4))) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            services = result.data.map(::toServiceItem),
                            isServicesLoading = false,
                            servicesError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            services = emptyList(),
                            isServicesLoading = false,
                            servicesError = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadTopWorkers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTopWorkersLoading = true, topWorkersError = null) }
            when (val result = getTopWorkersUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            topWorkers = result.data.map(::toTopWorkerItem),
                            isTopWorkersLoading = false,
                            topWorkersError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            topWorkers = emptyList(),
                            isTopWorkersLoading = false,
                            topWorkersError = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadBlogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBlogsLoading = true, blogsError = null) }
            when (val result = getArtikelListUseCase(limit = 5)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            blogs = result.data.map(::toBlogItem),
                            isBlogsLoading = false,
                            blogsError = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            blogs = emptyList(),
                            isBlogsLoading = false,
                            blogsError = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun toServiceItem(jasa: Jasa) = ServiceItem(
        id = jasa.id,
        title = jasa.title,
        price = CurrencyFormatter.formatRupiah(jasa.price),
        category = jasa.kategoriName.ifBlank { "Jasa" },
        workerName = jasa.workerName.ifBlank { "Freelancer" },
        rating = jasa.rating.toFloat().takeIf { it > 0f } ?: jasa.workerRating.toFloat(),
        imageUrl = jasa.imageUrl
    )

    private fun toProjectItem(project: Project) = ProjectItem(
        id = project.id,
        title = project.title,
        budget = CurrencyFormatter.formatRupiah(project.budget),
        category = project.kategoriName.ifBlank { "Proyek" },
        deadline = formatProjectDeadline(project.deadline),
        clientName = project.clientName.ifBlank { "Klien" },
        imageUrl = project.imageUrl
    )

    private fun toTopWorkerItem(worker: TopWorker) = TopWorkerItem(
        id = worker.mahasiswaId,
        userId = worker.userId,
        name = worker.displayName,
        skill = worker.skills.firstOrNull() ?: worker.university.ifBlank { "Freelancer IT" },
        rating = worker.rating,
        projectsDone = worker.completedOrders,
        avatarUrl = worker.avatarUrl
    )

    private fun toBlogItem(artikel: Artikel) = BlogItem(
        id = artikel.id,
        title = artikel.title,
        category = "BLOG",
        readTime = estimateReadTime(artikel.content),
        date = formatPublishedDate(artikel.publishedAt)
    )

    private fun estimateReadTime(content: String): String {
        val minutes = (content.split(Regex("\\s+")).count { it.isNotBlank() } / 200).coerceAtLeast(1)
        return "$minutes menit"
    }

    private fun formatPublishedDate(publishedAt: String?): String {
        if (publishedAt.isNullOrBlank()) return ""
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            Instant.parse(publishedAt).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault("")
    }

    private fun formatProjectDeadline(deadline: String): String {
        if (deadline.isBlank()) return "-"
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            Instant.parse(deadline).atZone(ZoneId.systemDefault()).format(formatter)
        }.getOrDefault(deadline.take(10))
    }
}
