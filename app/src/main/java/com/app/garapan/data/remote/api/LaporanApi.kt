package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.CreateLaporanDto
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST

interface LaporanApi {
    @POST("laporan")
    suspend fun createLaporan(@Body body: CreateLaporanDto): JsonElement
}
