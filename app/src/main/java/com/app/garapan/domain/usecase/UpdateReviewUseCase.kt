package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.UpdateReviewParams
import com.app.garapan.domain.repository.ReviewRepository
import javax.inject.Inject

class UpdateReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(params: UpdateReviewParams) =
        reviewRepository.updateReview(params)
}
