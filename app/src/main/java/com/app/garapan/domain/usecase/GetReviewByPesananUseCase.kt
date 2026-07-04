package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ReviewRepository
import javax.inject.Inject

class GetReviewByPesananUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(pesananId: String) =
        reviewRepository.getReviewByPesanan(pesananId)
}
