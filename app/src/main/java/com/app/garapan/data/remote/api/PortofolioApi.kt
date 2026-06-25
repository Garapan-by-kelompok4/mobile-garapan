package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.PortofolioDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PortofolioApi {
    @GET("portofolio/{mahasiswaId}")
    suspend fun getPortofolioList(@Path("mahasiswaId") mahasiswaId: String): List<PortofolioDto>

    @Multipart
    @POST("portofolio")
    suspend fun createPortofolio(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("projectUrl") projectUrl: RequestBody? = null
    ): PortofolioDto

    @DELETE("portofolio/{id}")
    suspend fun deletePortofolio(@Path("id") id: String)
}
