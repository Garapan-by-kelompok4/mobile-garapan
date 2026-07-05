package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.CreateContentReportDto
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST

interface ContentReportApi {
    @POST("content-reports")
    suspend fun createContentReport(@Body body: CreateContentReportDto): JsonElement
}
