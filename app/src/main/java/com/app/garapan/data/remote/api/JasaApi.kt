package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.JasaDto
import com.app.garapan.data.remote.dto.JasaListResponseDto
import com.app.garapan.data.remote.dto.UpdateJasaRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface JasaApi {
    @GET("jasa")
    suspend fun getJasaList(
        @Query("search") search: String? = null,
        @Query("kategoriId") kategoriId: String? = null,
        @Query("mahasiswaId") mahasiswaId: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sort") sort: String? = null,
        @Query("includeRelatedSkills") includeRelatedSkills: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): JasaListResponseDto

    @GET("jasa/mine")
    suspend fun getMyJasaList(
        @Query("search") search: String? = null,
        @Query("kategoriId") kategoriId: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): JasaListResponseDto

    @GET("jasa/{id}")
    suspend fun getJasaDetail(@Path("id") id: String): JasaDto

    @Multipart
    @POST("jasa")
    suspend fun createJasa(
        @Part("kategoriId") kategoriId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part image: MultipartBody.Part
    ): JasaDto

    @PATCH("jasa/{id}")
    suspend fun updateJasa(
        @Path("id") id: String,
        @Body body: UpdateJasaRequest
    ): JasaDto

    @DELETE("jasa/{id}")
    suspend fun deleteJasa(@Path("id") id: String)
}
