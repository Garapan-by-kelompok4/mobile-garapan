package com.app.garapan.presentation.screen.all_reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Review
import com.app.garapan.domain.usecase.GetReviewsUseCase
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

data class AllReviewItem(
    val id: String,
    val reviewerName: String,
    val date: String,
    val rating: Int,
    val comment: String
)

data class AllReviewsUiState(
    val reviews: List<AllReviewItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AllReviewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getReviewsUseCase: GetReviewsUseCase
) : ViewModel() {

    private val jasaId: String = savedStateHandle["jasaId"] ?: ""

    private val _uiState = MutableStateFlow(AllReviewsUiState(isLoading = true))
    val uiState: StateFlow<AllReviewsUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    fun retry() = loadReviews()

    private fun loadReviews() {
        if (jasaId.isBlank()) {
            _uiState.value = AllReviewsUiState(errorMessage = "Jasa tidak ditemukan.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getReviewsUseCase(jasaId)) {
                is Resource.Success -> {
                    _uiState.value = AllReviewsUiState(
                        reviews = result.data.map { it.toItem() },
                        isLoading = false
                    )
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

    private fun Review.toItem() = AllReviewItem(
        id = id,
        reviewerName = reviewerName.ifBlank { "Klien" },
        date = formatReviewDate(createdAt),
        rating = rating,
        comment = comment
    )

    private fun formatReviewDate(isoDate: String): String {
        if (isoDate.isBlank()) return ""
        return runCatching {
            val date = Instant.parse(isoDate).atZone(ZoneId.systemDefault()).toLocalDate()
            date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID")))
        }.getOrDefault(isoDate)
    }
}
