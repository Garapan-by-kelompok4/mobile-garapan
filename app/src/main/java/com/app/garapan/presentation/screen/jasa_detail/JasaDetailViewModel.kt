package com.app.garapan.presentation.screen.jasa_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaStatus
import com.app.garapan.domain.model.Review
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.usecase.GetJasaDetailUseCase
import com.app.garapan.domain.usecase.GetReviewsUseCase
import com.app.garapan.domain.usecase.ObserveCurrentUserUseCase
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.presentation.util.UserMessageLocalizer
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
    val reviewsErrorMessage: String? = null,
    val isOwner: Boolean = false,
    val isKlien: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class JasaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJasaDetailUseCase: GetJasaDetailUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    private val jasaId: String = savedStateHandle["jasaId"] ?: ""

    private var currentUserId: String? = null
    private var currentMahasiswaId: String? = null
    private var currentRole: Role? = null
    private var loadedJasa: Jasa? = null
    private var loadedReviews: List<Review> = emptyList()
    private var reviewsErrorMessage: String? = null

    private val _uiState = MutableStateFlow(JasaDetailUiState(isLoading = true))
    val uiState: StateFlow<JasaDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentUserUseCase().collect { user ->
                currentUserId = user?.id
                currentMahasiswaId = user?.mahasiswa?.id
                currentRole = user?.role
                loadedJasa?.let { jasa ->
                    _uiState.value = jasa.toUiState(
                        isOwner = isJasaOwner(jasa),
                        reviews = loadedReviews,
                        reviewError = reviewsErrorMessage
                    )
                }
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null, reviewsErrorMessage = null) }
            when (val result = getJasaDetailUseCase(jasaId)) {
                is Resource.Success -> {
                    loadedJasa = result.data
                    loadedReviews = emptyList()
                    reviewsErrorMessage = null
                    _uiState.value = result.data.toUiState(
                        isOwner = isJasaOwner(result.data),
                        reviews = loadedReviews,
                        reviewError = null
                    )
                    loadReviews(result.data)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun loadReviews(jasa: Jasa) {
        when (val result = getReviewsUseCase(jasa.id)) {
            is Resource.Success -> {
                loadedReviews = result.data
                reviewsErrorMessage = null
                _uiState.value = jasa.toUiState(
                    isOwner = isJasaOwner(jasa),
                    reviews = result.data,
                    reviewError = null
                )
            }
            is Resource.Error -> {
                reviewsErrorMessage = UserMessageLocalizer.localize(result.message)
                _uiState.update {
                    it.copy(
                        reviewsErrorMessage = reviewsErrorMessage,
                        isLoading = false
                    )
                }
            }
            Resource.Loading -> Unit
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

    private fun Jasa.toUiState(
        isOwner: Boolean,
        reviews: List<Review>,
        reviewError: String?
    ): JasaDetailUiState {
        val workerSubtitle = buildList {
            if (kategoriName.isNotBlank()) add(kategoriName)
            if (workerUniversity.isNotBlank()) add(workerUniversity)
        }.joinToString(" - ").ifBlank { "Mahasiswa IT" }

        val reviewItems = reviews.map { review ->
            JasaReviewItem(
                reviewerName = review.reviewerName.ifBlank { "Klien" },
                date = formatReviewDate(review.createdAt),
                rating = review.rating,
                comment = review.comment
            )
        }
        val liveRating = reviews.takeIf { it.isNotEmpty() }
            ?.map { it.rating }
            ?.average()
            ?.toFloat()
        val fallbackRating = rating.toFloat().takeIf { it > 0f } ?: workerRating.toFloat()

        return JasaDetailUiState(
            id = id,
            title = title,
            rating = liveRating ?: fallbackRating,
            reviewCount = reviews.size.takeIf { it > 0 } ?: reviewCount,
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
            reviews = reviewItems,
            ratingBreakdown = reviews
                .groupingBy { it.rating.coerceIn(1, 5) }
                .eachCount(),
            reviewsErrorMessage = reviewError,
            isOwner = isOwner,
            isKlien = currentRole == Role.KLIEN,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun formatReviewDate(isoDate: String): String {
        if (isoDate.isBlank()) return ""
        return runCatching {
            val date = Instant.parse(isoDate).atZone(ZoneId.systemDefault()).toLocalDate()
            date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")))
        }.getOrDefault(isoDate)
    }
}
