package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.CreateReviewRequest
import com.app.garapan.data.remote.dto.ReviewDto
import com.app.garapan.data.remote.dto.UpdateReviewRequest
import com.app.garapan.domain.model.CreateReviewParams
import com.app.garapan.domain.model.Review
import com.app.garapan.domain.model.UpdateReviewParams

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    pesananId = pesananId ?: pesanan?.id.orEmpty(),
    jasaId = pesanan?.jasaId,
    reviewerId = reviewerId.orEmpty(),
    reviewerName = reviewer.resolveDisplayName(),
    rating = rating.coerceIn(1, 5),
    comment = comment,
    createdAt = createdAt.orEmpty()
)

private fun com.app.garapan.data.remote.dto.ReviewUserDto?.resolveDisplayName(): String {
    if (this == null) return "Klien"
    return displayName
        ?: name
        ?: email
        ?: "Klien"
}

fun CreateReviewParams.toRequest(): CreateReviewRequest = CreateReviewRequest(
    pesananId = pesananId,
    rating = rating,
    comment = comment
)

fun UpdateReviewParams.toRequest(): UpdateReviewRequest = UpdateReviewRequest(
    rating = rating,
    comment = comment
)
