package com.app.garapan.presentation.screen.jasa_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class JasaFeatureItem(
    val title: String,
    val description: String
)

data class JasaPortfolioItem(
    val title: String,
    val category: String,
    val year: String
)

data class JasaReviewItem(
    val reviewerName: String,
    val date: String,
    val rating: Int,
    val comment: String
)

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
    val description: String = "",
    val techStack: List<String> = emptyList(),
    val features: List<JasaFeatureItem> = emptyList(),
    val portfolios: List<JasaPortfolioItem> = emptyList(),
    val reviews: List<JasaReviewItem> = emptyList(),
    val ratingBreakdown: Map<Int, Int> = emptyMap()
)

private val dummyDetails = mapOf(
    "1" to JasaDetailUiState(
        id = "1",
        title = "Platform e-Learning Interaktif untuk Bimbingan Belajar",
        rating = 4.9f,
        reviewCount = 128,
        isVerified = true,
        price = "Rp 2.500.000",
        priceShort = "Rp 2.5 jt",
        workerName = "Ahmad Sumbul",
        workerRole = "Mahasiswa Teknik",
        workerRating = 4.9f,
        description = "Dapatkan website e-commerce modern yang tidak hanya cantik secara visual, tapi juga dioptimalkan untuk konversi penjualan tinggi. Kami menggunakan stack teknologi terbaru untuk memastikan performa yang cepat dan aman.",
        techStack = listOf("React.js", "Tailwind CSS", "Node.js", "Stripe/Midtrans"),
        features = listOf(
            JasaFeatureItem("High Performance", "Load time di bawah 2 detik dengan optimasi aset modern dan lazy loading"),
            JasaFeatureItem("Responsive Design", "Tampilan sempurna di semua perangkat, dari mobile hingga desktop"),
            JasaFeatureItem("SEO Optimized", "Struktur kode dan konten yang ramah mesin pencari Google")
        ),
        portfolios = listOf(
            JasaPortfolioItem("Machine Learning", "E-Commerce", "2025"),
            JasaPortfolioItem("Organic Store", "E-Commerce", "2025"),
            JasaPortfolioItem("Fashion Hub", "Landing Page", "2024")
        ),
        reviews = listOf(
            JasaReviewItem("Budi Santoso", "12 Okt 2024", 5, "Hasilnya sangat memuaskan! Websitenya cepat dan desainnya modern. Komunikasi dengan Mas Aris juga sangat lancar dan sangat membantu saat revisi."),
            JasaReviewItem("Dina Amelia", "5 Sep 2024", 4, "Sangat direkomendasikan untuk pembuatan e-commerce. Integrasi payment gateway berjalan mulus tanpa kendala. Pengerjaan juga sesuai deadline."),
            JasaReviewItem("Rizky Pratama", "28 Agu 2024", 5, "Awalnya ragu karena harganya cukup terjangkau, tapi ternyata kualitasnya sekelas agency profesional. Admin panelnya gampang banget dipake. Mantap!")
        ),
        ratingBreakdown = mapOf(5 to 85, 4 to 30, 3 to 13)
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
        description = "Jasa pembuatan aplikasi mobile cross-platform menggunakan Flutter atau React Native. Hasil berupa aplikasi yang siap dipublikasikan ke Google Play Store dan Apple App Store.",
        techStack = listOf("Flutter", "Dart", "Firebase", "REST API"),
        features = listOf(
            JasaFeatureItem("Cross Platform", "Satu kode untuk Android dan iOS dengan performa native"),
            JasaFeatureItem("Push Notification", "Notifikasi real-time menggunakan Firebase Cloud Messaging")
        ),
        portfolios = listOf(
            JasaPortfolioItem("Toko Online App", "Mobile App", "2025"),
            JasaPortfolioItem("Delivery Tracker", "Mobile App", "2024")
        ),
        reviews = listOf(
            JasaReviewItem("Andi Setiawan", "3 Nov 2024", 5, "Aplikasinya berjalan sangat smooth di Android dan iOS. Komunikasi responsif dan revisi cepat."),
            JasaReviewItem("Maya Sari", "20 Okt 2024", 4, "Hasil memuaskan, fitur sesuai yang diminta. Waktu pengerjaan sedikit melebihi estimasi tapi kualitas oke.")
        ),
        ratingBreakdown = mapOf(5 to 10, 4 to 5, 3 to 2)
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
        description = "Jasa desain UI/UX profesional untuk website dan aplikasi mobile. Menggunakan Figma dengan pendekatan user-centered design untuk memastikan tampilan yang menarik dan pengalaman pengguna yang optimal.",
        techStack = listOf("Figma", "Adobe XD", "Protopie"),
        features = listOf(
            JasaFeatureItem("Wireframe & Prototype", "Mockup interaktif siap presentasi ke stakeholder"),
            JasaFeatureItem("Design System", "Komponen reusable dan panduan aset untuk developer")
        ),
        portfolios = listOf(
            JasaPortfolioItem("FinTech Dashboard", "Web Design", "2025"),
            JasaPortfolioItem("Health App", "Mobile Design", "2024")
        ),
        reviews = listOf(
            JasaReviewItem("Hendra Wijaya", "8 Des 2024", 5, "Desainnya modern banget dan sesuai brief. Revisi cepat dan komunikatif. Highly recommended!"),
            JasaReviewItem("Putri Rahayu", "1 Des 2024", 5, "Hasil luar biasa! Developer kami langsung bisa implement karena design system-nya lengkap.")
        ),
        ratingBreakdown = mapOf(5 to 35, 4 to 5, 3 to 1)
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
        description = "Pembuatan dashboard analitik interaktif menggunakan Python, SQL, dan tools visualisasi seperti Tableau atau Power BI. Dashboard mencakup laporan otomatis, grafik tren, dan filter data dinamis.",
        techStack = listOf("Python", "SQL", "Tableau", "Power BI"),
        features = listOf(
            JasaFeatureItem("Laporan Otomatis", "Export PDF/Excel terjadwal langsung ke email"),
            JasaFeatureItem("Filter Dinamis", "Analisis data berdasarkan periode, produk, dan wilayah")
        ),
        portfolios = listOf(
            JasaPortfolioItem("Sales Analytics", "Dashboard", "2025")
        ),
        reviews = listOf(
            JasaReviewItem("Teguh Prasetyo", "15 Nov 2024", 4, "Dashboard-nya informatif dan mudah dipahami manajemen. Sedikit revisi di awal tapi hasil akhir memuaskan.")
        ),
        ratingBreakdown = mapOf(5 to 5, 4 to 3, 3 to 1)
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
