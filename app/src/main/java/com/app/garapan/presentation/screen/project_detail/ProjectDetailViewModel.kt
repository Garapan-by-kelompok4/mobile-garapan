package com.app.garapan.presentation.screen.project_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProjectDetailUiState(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val deadline: String = "",
    val budget: String = "",
    val teamSize: String = "",
    val clientName: String = "",
    val clientType: String = "",
    val isVerified: Boolean = true,
    val description: String = ""
)

private val dummyDetails = mapOf(
    "1" to ProjectDetailUiState(
        id = "1",
        category = "Ed-Tech",
        title = "Platform e-Learning Interaktif untuk Bimbingan Belajar",
        deadline = "20 April 2026",
        budget = "Rp 5.000.000 - Rp 8.000.000",
        teamSize = "Tim (2-3 Orang)",
        clientName = "Indo Sejahtera Global",
        clientType = "Startup Ed Tech",
        isVerified = true,
        description = "Kami mencari talenta mahasiswa berbakat untuk membantu membangun platform e-learning interaktif yang akan digunakan oleh ribuan pelajar di seluruh Indonesia.\n\nPlatform ini akan mencakup fitur video streaming, quiz interaktif, dan sistem pelacakan progres belajar. Kandidat ideal memiliki pengalaman di bidang pengembangan web atau mobile, serta semangat tinggi untuk berkontribusi dalam dunia pendidikan digital."
    ),
    "2" to ProjectDetailUiState(
        id = "2",
        category = "Mobile Dev",
        title = "Aplikasi Manajemen Inventaris Gudang",
        deadline = "18 Mei 2026",
        budget = "Rp 3.000.000 - Rp 5.000.000",
        teamSize = "Tim (1-2 Orang)",
        clientName = "CV Berkah Mandiri",
        clientType = "Perusahaan Distribusi",
        isVerified = true,
        description = "Dibutuhkan developer untuk membangun aplikasi manajemen inventaris gudang berbasis mobile. Aplikasi mencakup fitur scan barcode, laporan stok real-time, dan notifikasi stok menipis.\n\nKami membutuhkan developer yang berpengalaman dengan React Native atau Flutter dan pernah menangani proyek serupa."
    ),
    "3" to ProjectDetailUiState(
        id = "3",
        category = "DevOps",
        title = "Sistem Monitoring Jaringan Real-Time",
        deadline = "10 Juni 2026",
        budget = "Rp 4.000.000 - Rp 6.000.000",
        teamSize = "Individu",
        clientName = "PT Teknologi Maju",
        clientType = "Perusahaan IT",
        isVerified = true,
        description = "Proyek ini mencakup pembuatan sistem monitoring jaringan berbasis web yang dapat memantau performa server, bandwidth, dan memberikan alert otomatis ketika terjadi gangguan.\n\nDibutuhkan keahlian di bidang networking, Linux, dan tools monitoring seperti Grafana atau Prometheus."
    ),
    "4" to ProjectDetailUiState(
        id = "4",
        category = "Data Science",
        title = "Dashboard Analitik Data Penjualan",
        deadline = "30 April 2026",
        budget = "Rp 2.500.000 - Rp 4.000.000",
        teamSize = "Tim (1-2 Orang)",
        clientName = "Startup Kopi",
        clientType = "F&B Startup",
        isVerified = false,
        description = "Kami membutuhkan dashboard analitik interaktif untuk memvisualisasikan data penjualan harian, mingguan, dan bulanan. Dashboard harus dapat menghasilkan laporan otomatis dan menampilkan tren penjualan produk.\n\nDibutuhkan keahlian Python, SQL, dan tools visualisasi seperti Tableau atau Power BI."
    )
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: String = savedStateHandle["projectId"] ?: "1"

    private val _uiState = MutableStateFlow(
        dummyDetails[projectId] ?: dummyDetails["1"]!!
    )
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()
}
