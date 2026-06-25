package com.app.garapan.data.remote.dto

data class ReviewDto(
    val id: String,
    val pesananId: String? = null,
    val reviewerId: String? = null,
    val rating: Int,
    val comment: String,
    val createdAt: String? = null,
    val reviewer: ReviewUserDto? = null,
    val pesanan: ReviewPesananDto? = null
)

data class ReviewUserDto(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val displayName: String? = null
)

data class ReviewPesananDto(
    val id: String? = null,
    val jasaId: String? = null
)

data class CreateReviewRequest(
    val pesananId: String,
    val rating: Int,
    val comment: String
)

data class UpdateReviewRequest(
    val rating: Int,
    val comment: String
)
