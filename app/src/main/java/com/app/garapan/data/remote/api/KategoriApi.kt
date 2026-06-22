package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.KategoriDto
import retrofit2.http.GET

interface KategoriApi {
    @GET("kategori")
    suspend fun getKategoriList(): List<KategoriDto>
}
