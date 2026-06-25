package com.app.garapan.presentation.screen.portfolio

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Portofolio
import com.app.garapan.domain.usecase.DeletePortofolioUseCase
import com.app.garapan.domain.usecase.GetPortofolioUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.ui.theme.AccentBlue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioItem(
    val id: String,
    val title: String,
    val description: String,
    val tags: List<String>,
    val imageUrl: String,
    val coverColor: Color,
    val accentColor: Color,
    val mockupTitle: String,
    val mockupSubtitle: String
)

data class PortfolioUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<PortfolioItem> = emptyList()
)

private val coverPalettes = listOf(
    Color(0xFFFF8A1F) to AccentBlue,
    Color(0xFFFF695F) to Color(0xFF1F2937),
    Color(0xFF0F3D49) to Color(0xFFB7D9DF)
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val getPortofolioUseCase: GetPortofolioUseCase,
    private val deletePortofolioUseCase: DeletePortofolioUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val mahasiswaId: String? = observeCurrentUserUseCase.snapshot()?.mahasiswa?.id

    private val _uiState = MutableStateFlow(PortfolioUiState(isLoading = true))
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        loadPortofolio()
    }

    fun loadPortofolio() {
        val id = mahasiswaId
        if (id.isNullOrBlank()) {
            _uiState.value = PortfolioUiState(
                isLoading = false,
                errorMessage = "Profil mahasiswa tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPortofolioUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = result.data.mapIndexed { index, item -> item.toUiItem(index) }
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onDeletePortfolio(portfolioId: String) {
        viewModelScope.launch {
            when (val result = deletePortofolioUseCase(portfolioId)) {
                is Resource.Success -> loadPortofolio()
                is Resource.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    private fun Portofolio.toUiItem(index: Int): PortfolioItem {
        val (coverColor, accentColor) = coverPalettes[index % coverPalettes.size]
        return PortfolioItem(
            id = id,
            title = title,
            description = description,
            tags = listOf("PORTOFOLIO"),
            imageUrl = imageUrl,
            coverColor = coverColor,
            accentColor = accentColor,
            mockupTitle = title.take(24),
            mockupSubtitle = projectUrl ?: "Portofolio"
        )
    }
}
