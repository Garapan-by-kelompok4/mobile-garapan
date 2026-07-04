package com.app.garapan.presentation.screen.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateReviewParams
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.UpdateReviewParams
import com.app.garapan.domain.usecase.GetPesananDetailUseCase
import com.app.garapan.domain.usecase.GetReviewByPesananUseCase
import com.app.garapan.domain.usecase.SubmitReviewUseCase
import com.app.garapan.domain.usecase.UpdateReviewUseCase
import com.app.garapan.presentation.util.PesananDisplayMapper
import com.app.garapan.presentation.util.UserMessageLocalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val pesananId: String = "",
    val jasaId: String = "",
    val jasaTitle: String = "",
    val workerName: String = "",
    val orderDate: String = "",
    val existingReviewId: String? = null,
    val isEditMode: Boolean = false,
    val rating: Int = 5,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitEnabled: Boolean = true
)

sealed interface ReviewEvent {
    data class Submitted(val isEditMode: Boolean) : ReviewEvent
    data class ShowMessage(val message: String) : ReviewEvent
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPesananDetailUseCase: GetPesananDetailUseCase,
    private val getReviewByPesananUseCase: GetReviewByPesananUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase,
    private val updateReviewUseCase: UpdateReviewUseCase
) : ViewModel() {

    private val pesananId: String = savedStateHandle["pesananId"] ?: ""

    private val _uiState = MutableStateFlow(ReviewUiState(isLoading = true))
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReviewEvent>()
    val events: SharedFlow<ReviewEvent> = _events.asSharedFlow()

    init {
        loadOrder()
    }

    fun retry() = loadOrder()

    fun onRatingChanged(rating: Int) {
        _uiState.update { it.copy(rating = rating.coerceIn(1, 5), errorMessage = null) }
    }

    fun onCommentChanged(comment: String) {
        _uiState.update {
            it.copy(
                comment = comment.take(500),
                errorMessage = null
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val trimmedComment = state.comment.trim()
        if (state.pesananId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pesanan tidak valid untuk diulas.") }
            return
        }
        if (trimmedComment.length < 10) {
            _uiState.update { it.copy(errorMessage = "Tulis ulasan minimal 10 karakter.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = if (state.isEditMode && !state.existingReviewId.isNullOrBlank()) {
                updateReviewUseCase(
                    UpdateReviewParams(
                        id = state.existingReviewId,
                        rating = state.rating,
                        comment = trimmedComment
                    )
                )
            } else {
                submitReviewUseCase(
                    CreateReviewParams(
                        pesananId = state.pesananId,
                        rating = state.rating,
                        comment = trimmedComment
                    )
                )
            }
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.emit(ReviewEvent.Submitted(state.isEditMode))
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadOrder() {
        if (pesananId.isBlank()) {
            _uiState.value = ReviewUiState(
                isLoading = false,
                isSubmitEnabled = false,
                errorMessage = "Pesanan tidak ditemukan."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPesananDetailUseCase(pesananId)) {
                is Resource.Success -> {
                    val pesanan = result.data
                    val canReview = pesanan.status == PesananStatus.COMPLETED
                    val baseState = ReviewUiState(
                        pesananId = pesanan.id,
                        jasaId = pesanan.jasaId.orEmpty(),
                        jasaTitle = PesananDisplayMapper.orderTitle(pesanan.jasaTitle, pesanan.projectId),
                        workerName = pesanan.workerName,
                        orderDate = PesananDisplayMapper.formatOrderDate(pesanan.createdAt),
                        isLoading = false,
                        isSubmitEnabled = canReview,
                        errorMessage = if (canReview) null else "Ulasan hanya bisa dikirim untuk pesanan jasa yang sudah selesai."
                    )
                    _uiState.value = baseState
                    if (canReview) {
                        loadExistingReview(baseState)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSubmitEnabled = false,
                            errorMessage = UserMessageLocalizer.localize(result.message)
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun loadExistingReview(baseState: ReviewUiState) {
        when (val result = getReviewByPesananUseCase(baseState.pesananId)) {
            is Resource.Success -> {
                val existingReview = result.data
                _uiState.update {
                    it.copy(
                        existingReviewId = existingReview.id,
                        isEditMode = true,
                        rating = existingReview.rating,
                        comment = existingReview.comment,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            is Resource.Error -> Unit
            Resource.Loading -> Unit
        }
    }
}
