package com.app.garapan.presentation.screen.blog_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed class BlogBodyBlock {
    data class Paragraph(val text: String) : BlogBodyBlock()
    data class Heading(val number: Int, val text: String) : BlogBodyBlock()
    data class Quote(val text: String) : BlogBodyBlock()
}

data class RecommendationItem(
    val id: String,
    val category: String,
    val title: String,
    val excerpt: String
)

data class BlogDetailUiState(
    val blogId: String = "",
    val category: String = "TIPS & KARIR",
    val date: String = "14 April 2026",
    val title: String = "5 Tips Sukses Freelance di Bidang IT untuk Mahasiswa",
    val authorName: String = "Admin GARAPAN",
    val authorRole: String = "Tech & Career Editor",
    val readTime: String = "3 menit baca",
    val heroSubtitle: String = "Panduan lengkap membangun karir freelance IT sejak bangku kuliah",
    val body: List<BlogBodyBlock> = listOf(
        BlogBodyBlock.Paragraph("Dunia freelance IT terbuka lebar bagi mahasiswa yang ingin mendapatkan penghasilan sambil mengasah skill. Namun tanpa strategi yang tepat, banyak yang menyerah di tengah jalan."),
        BlogBodyBlock.Heading(1, "Bangun Portofolio Sejak Awal"),
        BlogBodyBlock.Paragraph("Portofolio adalah aset utama seorang freelancer. Mulailah dengan proyek kecil — bahkan proyek kampus pun bisa dijadikan showcase yang menarik bagi klien pertama Anda."),
        BlogBodyBlock.Quote("Klien tidak membeli janji, mereka membeli bukti. Portofolio adalah bukti terbaik yang bisa kamu tunjukkan."),
        BlogBodyBlock.Heading(2, "Tetapkan Harga yang Realistis"),
        BlogBodyBlock.Paragraph("Banyak mahasiswa undervalue diri sendiri. Riset harga pasar, hitung biaya waktu dan tenaga, lalu tentukan tarif yang adil — bukan sekadar murah.")
    ),
    val recommendations: List<RecommendationItem> = listOf(
        RecommendationItem("r1", "BISNIS", "Cara Menetapkan Harga Jasa yang Tepat", "Strategi penetapan harga untuk freelancer pemula agar tetap kompetitif."),
        RecommendationItem("r2", "TREN", "Teknologi Yang Paling Banyak Dicari Klien 2025", "Skill IT terpanas yang wajib dikuasai untuk memenangkan lebih banyak proyek.")
    )
)

@HiltViewModel
class BlogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlogDetailUiState())
    val uiState: StateFlow<BlogDetailUiState> = _uiState.asStateFlow()

    init {
        val blogId = savedStateHandle.get<String>("blogId") ?: ""
        _uiState.update { it.copy(blogId = blogId) }
    }
}
