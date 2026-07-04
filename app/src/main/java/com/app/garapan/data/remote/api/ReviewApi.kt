package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.CreateReviewRequest
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewApi {
    @GET("review/{jasaId}")
    suspend fun getReviews(@Path("jasaId") jasaId: String): JsonElement

    @GET("review/pesanan/{pesananId}")
    suspend fun getReviewByPesanan(@Path("pesananId") pesananId: String): JsonElement

    @POST("review")
    suspend fun submitReview(@Body body: CreateReviewRequest): JsonElement

    @PATCH("review/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body body: com.app.garapan.data.remote.dto.UpdateReviewRequest
    ): JsonElement
}
