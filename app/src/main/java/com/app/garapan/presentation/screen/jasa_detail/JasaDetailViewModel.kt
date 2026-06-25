package com.app.garapan.presentation.screen.jasa_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.GetJasaDetailUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JasaFeatureItem(
    val title: String,
    val description: String
)

data class JasaPortfolioItem(
    val title: String,
    val category: String,
    val year: String,
    val imageUrl: String = ""
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
    val imageUrl: String = "",
    val workerId: String = "",
    val workerUserId: String = "",
    val workerName: String = "",
    val workerRole: String = "",
    val workerAvatarUrl: String = "",
    val workerRating: Float = 0f,
    val description: String = "",
    val techStack: List<String> = emptyList(),
    val features: List<JasaFeatureItem> = emptyList(),
    val portfolios: List<JasaPortfolioItem> = emptyList(),
    val reviews: List<JasaReviewItem> = emptyList(),
    val ratingBreakdown: Map<Int, Int> = emptyMap(),
    val isOwner: Boolean = false,
    val isKlien: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JasaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJasaDetailUseCase: GetJasaDetailUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val jasaId: String = savedStateHandle["jasaId"] ?: ""

    private var currentUserId: String? = null
    private var currentMahasiswaId: String? = null
    private var currentRole: Role? = null
    private var loadedJasa: Jasa? = null

    private val _uiState = MutableStateFlow(JasaDetailUiState(isLoading = true))
    val uiState: StateFlow<JasaDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentUserId = user?.id
                currentMahasiswaId = user?.mahasiswa?.id
                currentRole = user?.role
                loadedJasa?.let { jasa -> _uiState.value = jasa.toUiState(isJasaOwner(jasa)) }
            }
        }
        loadJasaDetail()
    }

    fun retry() = loadJasaDetail()

    private fun loadJasaDetail() {
        if (jasaId.isBlank()) {
            _uiState.value = JasaDetailUiState(
                isLoading = false,
                errorMessage = "Jasa tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getJasaDetailUseCase(jasaId)) {
                is Resource.Success -> {
                    loadedJasa = result.data
                    _uiState.value = result.data.toUiState(isJasaOwner(result.data))
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

    private fun isJasaOwner(jasa: Jasa): Boolean {
        val userId = currentUserId
        if (!userId.isNullOrBlank() && jasa.workerUserId.isNotBlank() && jasa.workerUserId == userId) {
            return true
        }
        val mahasiswaId = currentMahasiswaId
        return !mahasiswaId.isNullOrBlank() &&
            jasa.mahasiswaId.isNotBlank() &&
            jasa.mahasiswaId == mahasiswaId
    }

    private fun Jasa.toUiState(isOwner: Boolean): JasaDetailUiState {
        val workerSubtitle = buildList {
            if (kategoriName.isNotBlank()) add(kategoriName)
            if (workerUniversity.isNotBlank()) add(workerUniversity)
        }.joinToString(" · ").ifBlank { "Mahasiswa IT" }

        return JasaDetailUiState(
            id = id,
            title = title,
            rating = rating.toFloat().takeIf { it > 0f } ?: workerRating.toFloat(),
            reviewCount = reviewCount,
            isVerified = status == JasaStatus.ACTIVE,
            price = CurrencyFormatter.formatRupiah(price),
            imageUrl = imageUrl,
            workerId = mahasiswaId,
            workerUserId = workerUserId,
            workerName = workerName.ifBlank { "Freelancer" },
            workerRole = workerSubtitle,
            workerAvatarUrl = workerAvatarUrl,
            workerRating = workerRating.toFloat(),
            description = description,
            portfolios = portfolios.map { item ->
                JasaPortfolioItem(
                    title = item.title,
                    category = kategoriName.ifBlank { "Portofolio" },
                    year = "",
                    imageUrl = item.imageUrl
                )
            },
            isOwner = isOwner,
            isKlien = currentRole == Role.KLIEN,
            isLoading = false,
            errorMessage = null
        )
    }
}
