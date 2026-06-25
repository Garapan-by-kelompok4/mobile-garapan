package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.ProjectDto
import com.app.garapan.data.remote.dto.ProjectListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectApi {
    @GET("project")
    suspend fun getProjectList(
        @Query("search") search: String? = null,
        @Query("kategori") kategori: String? = null,
        @Query("minBudget") minBudget: Double? = null,
        @Query("maxBudget") maxBudget: Double? = null,
        @Query("includeRelatedSkills") includeRelatedSkills: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ProjectListResponseDto

    @GET("project/{id}")
    suspend fun getProjectDetail(@Path("id") id: String): ProjectDto
}
