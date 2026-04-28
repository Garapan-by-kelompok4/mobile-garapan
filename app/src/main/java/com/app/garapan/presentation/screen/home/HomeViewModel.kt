package com.app.garapan.presentation.screen.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProjectItem(
    val id: String,
    val title: String,
    val budget: String,
    val category: String,
    val deadline: String,
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
    val name: String,
    val skill: String,
    val rating: Float,
    val projectsDone: Int
)

data class BlogItem(
    val id: String,
    val title: String,
    val category: String,
    val readTime: String
)

data class HomeUiState(
    val userName: String = "Praffi",
    val selectedNavIndex: Int = 0,
    val projects: List<ProjectItem> = emptyList(),
    val services: List<ServiceItem> = emptyList(),
    val activities: List<ActivityItem> = emptyList(),
    val topWorkers: List<TopWorkerItem> = emptyList(),
    val blogs: List<BlogItem> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            projects = listOf(
                ProjectItem("1", "Buat Website E-Commerce", "Rp 3.000.000", "Web Dev", "2 minggu", "PT Maju Jaya"),
                ProjectItem("2", "Desain UI Mobile App", "Rp 2.500.000", "UI/UX", "1 minggu", "Startup Kopi"),
                ProjectItem("3", "Integrasi API Payment", "Rp 1.800.000", "Backend", "3 hari", "TokoOnline.id"),
                ProjectItem("4", "Analisis Data Penjualan", "Rp 2.000.000", "Data Science", "1 minggu", "CV Berkah"),
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
            ),
            topWorkers = listOf(
                TopWorkerItem("1", "Andi Pratama", "UI/UX Design", 4.9f, 47),
                TopWorkerItem("2", "Sari Dewi", "Web Development", 4.8f, 63),
                TopWorkerItem("3", "Rizky Fajar", "DevOps", 5.0f, 29),
                TopWorkerItem("4", "Budi Santoso", "AI/ML", 4.7f, 38),
            ).sortedByDescending { it.rating },
            blogs = listOf(
                BlogItem("1", "5 Tips Sukses Freelance di Bidang IT", "Tips & Trik", "3 menit"),
                BlogItem("2", "Cara Menetapkan Harga Jasa yang Tepat", "Bisnis", "5 menit"),
                BlogItem("3", "Teknologi Yang Paling Banyak Dicari Klien 2025", "Tren", "4 menit"),
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onNavItemSelected(index: Int) = _uiState.value.let {
        _uiState.value = it.copy(selectedNavIndex = index)
    }
}
