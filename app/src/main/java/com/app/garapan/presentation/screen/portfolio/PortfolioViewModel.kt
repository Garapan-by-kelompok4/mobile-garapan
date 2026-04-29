package com.app.garapan.presentation.screen.portfolio

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.app.garapan.ui.theme.AccentBlue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PortfolioItem(
    val id: String,
    val title: String,
    val description: String,
    val tags: List<String>,
    val coverColor: Color,
    val accentColor: Color,
    val mockupTitle: String,
    val mockupSubtitle: String
)

data class PortfolioUiState(
    val items: List<PortfolioItem> = emptyList()
)

@HiltViewModel
class PortfolioViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        PortfolioUiState(
            items = listOf(
                PortfolioItem(
                    id = "portfolio-1",
                    title = "Redesign Website E-Commerce \"LokalKarya\"",
                    description = "Meningkatkan konversi penjualan sebesar 35% melalui perancangan ulang halaman produk dan checkout.",
                    tags = listOf("WEB DESIGN", "UI/UX"),
                    coverColor = Color(0xFFFF8A1F),
                    accentColor = AccentBlue,
                    mockupTitle = "Premium Design",
                    mockupSubtitle = "wesiigreesign"
                ),
                PortfolioItem(
                    id = "portfolio-2",
                    title = "Identitas Visual \"Kopi Senja\"",
                    description = "Pengembangan logo dan pedoman merek komprehensif untuk kedai kopi lokal.",
                    tags = listOf("BRANDING", "LOGO"),
                    coverColor = Color(0xFFFF695F),
                    accentColor = Color(0xFF1F2937),
                    mockupTitle = "Brand Identity",
                    mockupSubtitle = "Soft work"
                ),
                PortfolioItem(
                    id = "portfolio-3",
                    title = "Kampanye Digital \"TechFest 2023\"",
                    description = "Desain aset media sosial untuk festival teknologi yang menjangkau lebih dari ribuan audiens.",
                    tags = listOf("SOCIAL MEDIA", "GRAPHIC DESIGN"),
                    coverColor = Color(0xFF0F3D49),
                    accentColor = Color(0xFFB7D9DF),
                    mockupTitle = "SALETI TO WORK",
                    mockupSubtitle = "Digital Campaign"
                )
            )
        )
    )
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    fun onDeletePortfolio(portfolioId: String) {
        _uiState.update { state ->
            state.copy(items = state.items.filterNot { item -> item.id == portfolioId })
        }
    }
}
