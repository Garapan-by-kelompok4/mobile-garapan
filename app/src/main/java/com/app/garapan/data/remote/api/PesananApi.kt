package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.CreatePesananRequest
import com.app.garapan.data.remote.dto.PesananDto
import com.app.garapan.data.remote.dto.PesananListResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PesananApi {
    @POST("pesanan")
    suspend fun createPesanan(@Body body: CreatePesananRequest): PesananDto

    @GET("pesanan")
    suspend fun getMyPesananList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): PesananListResponseDto

    @GET("pesanan/{id}")
    suspend fun getPesananDetail(@Path("id") id: String): PesananDto

    @PATCH("pesanan/{id}/deliver")
    suspend fun deliverPesanan(@Path("id") id: String): PesananDto

    @PATCH("pesanan/{id}/complete")
    suspend fun completePesanan(@Path("id") id: String): PesananDto
}
