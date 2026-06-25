package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.CreateReviewParams
import com.app.garapan.domain.repository.ReviewRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(params: CreateReviewParams) =
        reviewRepository.submitReview(params)
}
