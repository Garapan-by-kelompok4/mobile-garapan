package com.app.garapan.domain.model

data class Review(
    val id: String,
    val pesananId: String,
    val jasaId: String?,
    val reviewerId: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

data class CreateReviewParams(
    val pesananId: String,
    val rating: Int,
    val comment: String
)

data class UpdateReviewParams(
    val id: String,
    val rating: Int,
    val comment: String
)
