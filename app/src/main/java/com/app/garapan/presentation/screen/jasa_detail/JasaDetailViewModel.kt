package com.app.garapan.presentation.screen.jasa_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class JasaDetailUiState(
    val id: String = "",
    val title: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isVerified: Boolean = true,
    val price: String = "",
    val priceShort: String = "",
    val workerName: String = "",
    val workerRole: String = "",
    val workerRating: Float = 0f,
    val description: String = ""
)

private val dummyDetails = mapOf(
    "1" to JasaDetailUiState(
        id = "1",
        title = "Platform e-Learning Interaktif untuk Bimbingan Belajar",
        rating = 4.7f,
        reviewCount = 23,
        isVerified = true,
        price = "Rp 2.500.000",
        priceShort = "Rp 2.5 jt",
        workerName = "Ahmad Sumbul",
        workerRole = "Mahasiswa Teknik",
        workerRating = 4.7f,
        description = "Saya menawarkan jasa pembuatan platform e-learning interaktif yang mencakup fitur video streaming, quiz interaktif, dan sistem pelacakan progres belajar.\n\nPengalaman lebih dari 2 tahun di bidang pengembangan web dan mobile. Siap membantu bisnis Anda bertransformasi digital dengan solusi pendidikan yang inovatif dan mudah digunakan."
    ),
    "2" to JasaDetailUiState(
        id = "2",
        title = "Pembuatan Aplikasi Mobile Android & iOS Profesional",
        rating = 4.5f,
        reviewCount = 17,
        isVerified = true,
        price = "Rp 1.800.000",
        priceShort = "Rp 1.8 jt",
        workerName = "Rizky Pratama",
        workerRole = "Mahasiswa Informatika",
        workerRating = 4.5f,
        description = "Jasa pembuatan aplikasi mobile cross-platform menggunakan Flutter atau React Native. Hasil berupa aplikasi yang siap dipublikasikan ke Google Play Store dan Apple App Store.\n\nMeliputi desain UI/UX modern, integrasi API, autentikasi pengguna, dan notifikasi push. Revisi hingga klien puas."
    ),
    "3" to JasaDetailUiState(
        id = "3",
        title = "Desain UI/UX Website & Mobile App Modern",
        rating = 4.9f,
        reviewCount = 41,
        isVerified = true,
        price = "Rp 750.000",
        priceShort = "Rp 750 rb",
        workerName = "Sari Dewi",
        workerRole = "Mahasiswa Desain Komunikasi Visual",
        workerRating = 4.9f,
        description = "Jasa desain UI/UX profesional untuk website dan aplikasi mobile. Menggunakan Figma dengan pendekatan user-centered design untuk memastikan tampilan yang menarik dan pengalaman pengguna yang optimal.\n\nTermasuk wireframe, prototype interaktif, design system, dan panduan aset siap pakai untuk developer."
    ),
    "4" to JasaDetailUiState(
        id = "4",
        title = "Pembuatan Dashboard Analitik & Visualisasi Data",
        rating = 4.3f,
        reviewCount = 9,
        isVerified = false,
        price = "Rp 1.200.000",
        priceShort = "Rp 1.2 jt",
        workerName = "Budi Santoso",
        workerRole = "Mahasiswa Sistem Informasi",
        workerRating = 4.3f,
        description = "Pembuatan dashboard analitik interaktif menggunakan Python, SQL, dan tools visualisasi seperti Tableau atau Power BI. Dashboard mencakup laporan otomatis, grafik tren, dan filter data dinamis.\n\nCocok untuk UMKM dan startup yang ingin memantau performa bisnis secara real-time."
    )
)

@HiltViewModel
class JasaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jasaId: String = savedStateHandle["jasaId"] ?: "1"

    private val _uiState = MutableStateFlow(
        dummyDetails[jasaId] ?: dummyDetails["1"]!!
    )
    val uiState: StateFlow<JasaDetailUiState> = _uiState.asStateFlow()
}
