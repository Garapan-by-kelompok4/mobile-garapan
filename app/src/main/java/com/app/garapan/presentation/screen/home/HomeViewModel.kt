package com.app.garapan.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Artikel
import com.app.garapan.domain.model.TopWorker
import com.app.garapan.domain.usecase.GetArtikelListUseCase
import com.app.garapan.domain.usecase.GetTopWorkersUseCase
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
    val teamSize: String,
    val clientName: String
)

data class ServiceItem(
    val id: String,
    val title: String,
    val price: String,
    val category: String,
    val workerName: String,
    val rating: Float
)

data class ActivityItem(
    val id: String,
    val message: String,
    val timeAgo: String
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
    val userName: String = "Praffi",
    val selectedNavIndex: Int = 0,
    val projects: List<ProjectItem> = emptyList(),
    val services: List<ServiceItem> = emptyList(),
    val activities: List<ActivityItem> = emptyList(),
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
    private val getArtikelListUseCase: GetArtikelListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            projects = listOf(
                ProjectItem("1", "Platform e-Learning Interaktif untuk Bimbingan Belajar", "Rp 5.000.000 - Rp 8.000.000", "Ed-Tech", "20 April 2026", "Tim (2-3 Orang)", "PT Nusantara Global"),
                ProjectItem("2", "Aplikasi Manajemen Inventaris Gudang", "Rp 3.000.000 - Rp 5.000.000", "Mobile Dev", "18 Mei 2026", "Tim (1-2 Orang)", "CV Berkah Mandiri"),
                ProjectItem("3", "Sistem Monitoring Jaringan Real-Time", "Rp 4.000.000 - Rp 6.000.000", "DevOps", "10 Juni 2026", "Individu", "PT Teknologi Maju"),
                ProjectItem("4", "Dashboard Analitik Data Penjualan", "Rp 2.500.000 - Rp 4.000.000", "Data Science", "30 April 2026", "Tim (1-2 Orang)", "Startup Kopi"),
            ),
            services = listOf(
                ServiceItem("1", "Desain Logo Profesional", "Rp 500.000", "UI/UX", "Andi Pratama", 4.9f),
                ServiceItem("2", "Landing Page Modern", "Rp 1.200.000", "Web Dev", "Sari Dewi", 4.8f),
                ServiceItem("3", "Setup Server VPS", "Rp 800.000", "DevOps", "Rizky Fajar", 5.0f),
                ServiceItem("4", "Machine Learning Model", "Rp 3.500.000", "AI/ML", "Budi Santoso", 4.7f),
            ),
            activities = listOf(
                ActivityItem("1", "PT Maju Jaya membuka proyek baru: Buat Website E-Commerce", "5 menit lalu"),
                ActivityItem("2", "Andi Pratama menyelesaikan pesanan Desain Logo", "20 menit lalu"),
                ActivityItem("3", "Sari Dewi mendapat ulasan bintang 5 dari klien", "1 jam lalu"),
                ActivityItem("4", "Proyek baru: Integrasi Midtrans Payment Gateway", "2 jam lalu"),
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTopWorkers()
        loadBlogs()
    }

    fun onNavItemSelected(index: Int) = _uiState.value.let {
        _uiState.value = it.copy(selectedNavIndex = index)
    }

    fun retryTopWorkers() = loadTopWorkers()

    fun retryBlogs() = loadBlogs()

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
}
