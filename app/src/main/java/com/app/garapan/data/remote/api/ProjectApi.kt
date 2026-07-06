package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.CreateProjectRequest
import com.app.garapan.data.remote.dto.PesananDto
import com.app.garapan.data.remote.dto.ProjectDto
import com.app.garapan.data.remote.dto.ProjectListResponseDto
import com.app.garapan.data.remote.dto.ProjectProposalDto
import com.app.garapan.data.remote.dto.ProjectProposalListResponseDto
import com.app.garapan.data.remote.dto.SubmitProposalRequestDto
import com.app.garapan.data.remote.dto.UpdateProjectRequest
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

interface ProjectApi {
    @GET("project")
    suspend fun getProjectList(
        @Query("search") search: String? = null,
        @Query("kategori") kategori: String? = null,
        @Query("kategoriId") kategoriId: String? = null,
        @Query("minBudget") minBudget: Double? = null,
        @Query("maxBudget") maxBudget: Double? = null,
        @Query("sort") sort: String? = null,
        @Query("includeRelatedSkills") includeRelatedSkills: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ProjectListResponseDto

    @GET("project/mine/list")
    suspend fun getMyProjectList(
        @Query("search") search: String? = null,
        @Query("kategori") kategori: String? = null,
        @Query("kategoriId") kategoriId: String? = null,
        @Query("minBudget") minBudget: Double? = null,
        @Query("maxBudget") maxBudget: Double? = null,
        @Query("sort") sort: String? = null,
        @Query("includeRelatedSkills") includeRelatedSkills: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ProjectListResponseDto

    @GET("project/{id}")
    suspend fun getProjectDetail(@Path("id") id: String): ProjectDto

    @POST("project")
    suspend fun createProject(@Body body: CreateProjectRequest): ProjectDto

    @Multipart
    @POST("project")
    suspend fun createProjectMultipart(
        @Part("kategoriId") kategoriId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("budget") budget: RequestBody,
        @Part("minBudget") minBudget: RequestBody? = null,
        @Part("maxBudget") maxBudget: RequestBody? = null,
        @Part("deadline") deadline: RequestBody,
        @Part image: MultipartBody.Part
    ): ProjectDto

    @POST("project/{id}/proposals")
    suspend fun submitProposal(
        @Path("id") id: String,
        @Body body: SubmitProposalRequestDto
    ): ProjectProposalDto

    @DELETE("project/{id}/proposals/me")
    suspend fun withdrawProposal(@Path("id") id: String)

    @GET("project/{id}/proposals")
    suspend fun getProjectProposals(@Path("id") id: String): List<ProjectProposalDto>

    @GET("project/mine/proposals")
    suspend fun getMyProposals(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ProjectProposalListResponseDto

    @PATCH("project/{id}/proposals/{proposalId}/accept")
    suspend fun acceptProposal(
        @Path("id") id: String,
        @Path("proposalId") proposalId: String
    ): PesananDto

    @PATCH("project/{id}/proposals/{proposalId}/reject")
    suspend fun rejectProposal(
        @Path("id") id: String,
        @Path("proposalId") proposalId: String
    )

    @PATCH("project/{id}")
    suspend fun updateProject(
        @Path("id") id: String,
        @Body body: UpdateProjectRequest
    ): ProjectDto

    @Multipart
    @PATCH("project/{id}")
    suspend fun updateProjectMultipart(
        @Path("id") id: String,
        @Part("title") title: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("budget") budget: RequestBody? = null,
        @Part("minBudget") minBudget: RequestBody? = null,
        @Part("maxBudget") maxBudget: RequestBody? = null,
        @Part("deadline") deadline: RequestBody? = null,
        @Part("kategoriId") kategoriId: RequestBody? = null,
        @Part("status") status: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): ProjectDto

    @DELETE("project/{id}")
    suspend fun deleteProject(@Path("id") id: String)
}
