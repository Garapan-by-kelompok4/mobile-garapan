package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toRequest
import com.app.garapan.data.remote.api.ReviewApi
import com.app.garapan.data.remote.dto.ReviewDto
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateReviewParams
import com.app.garapan.domain.model.Review
import com.app.garapan.domain.model.UpdateReviewParams
import com.app.garapan.domain.repository.ReviewRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val reviewApi: ReviewApi,
    private val gson: Gson
) : ReviewRepository {

    override suspend fun getReviews(jasaId: String): Resource<List<Review>> =
        safeApiCall {
            parseReviewList(reviewApi.getReviews(jasaId)).map { it.toDomain() }
        }

    override suspend fun submitReview(params: CreateReviewParams): Resource<Review> =
        safeApiCall {
            parseReview(reviewApi.submitReview(params.toRequest())).toDomain()
        }

    override suspend fun updateReview(params: UpdateReviewParams): Resource<Review> =
        safeApiCall {
            parseReview(reviewApi.updateReview(params.id, params.toRequest())).toDomain()
        }

    private fun parseReviewList(json: JsonElement): List<ReviewDto> {
        val payload = json.unwrapData()
        if (payload.isJsonNull) return emptyList()
        val listType = object : TypeToken<List<ReviewDto>>() {}.type
        return gson.fromJson(payload, listType) ?: emptyList()
    }

    private fun parseReview(json: JsonElement): ReviewDto =
        gson.fromJson(json.unwrapData(), ReviewDto::class.java)

    private fun JsonElement.unwrapData(): JsonElement {
        val obj = takeIf { it.isJsonObject }?.asJsonObject
        return obj?.get("data") ?: this
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
