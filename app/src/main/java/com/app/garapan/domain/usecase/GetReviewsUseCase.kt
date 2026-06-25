package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ReviewRepository
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(jasaId: String) =
        reviewRepository.getReviews(jasaId)
}
