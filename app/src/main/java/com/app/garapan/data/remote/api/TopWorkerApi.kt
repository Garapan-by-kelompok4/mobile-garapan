package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.TopWorkerDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TopWorkerApi {
    @GET("top-worker")
    suspend fun getTopWorkers(
        @Query("period") period: String? = null
    ): List<TopWorkerDto>
}
