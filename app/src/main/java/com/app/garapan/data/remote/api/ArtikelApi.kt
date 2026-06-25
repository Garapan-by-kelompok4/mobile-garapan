package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.ArtikelDto
import com.app.garapan.data.remote.dto.ArtikelListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArtikelApi {
    @GET("artikel")
    suspend fun getArtikelList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ArtikelListResponseDto

    @GET("artikel/{id}")
    suspend fun getArtikelDetail(@Path("id") id: String): ArtikelDto
}
