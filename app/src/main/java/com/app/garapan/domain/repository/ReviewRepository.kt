package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateReviewParams
import com.app.garapan.domain.model.Review
import com.app.garapan.domain.model.UpdateReviewParams

interface ReviewRepository {
    suspend fun getReviews(jasaId: String): Resource<List<Review>>
    suspend fun getReviewByPesanan(pesananId: String): Resource<Review>
    suspend fun submitReview(params: CreateReviewParams): Resource<Review>
    suspend fun updateReview(params: UpdateReviewParams): Resource<Review>
}
