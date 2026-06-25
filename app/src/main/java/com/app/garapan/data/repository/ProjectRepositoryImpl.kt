package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toMultipartRequest
import com.app.garapan.data.mapper.toRequest
import com.app.garapan.data.remote.api.ProjectApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.model.UpdateProjectParams
import com.app.garapan.domain.repository.ProjectRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectApi: ProjectApi
) : ProjectRepository {

    override suspend fun getProjectList(filters: ProjectListFilters): Resource<List<Project>> =
        safeApiCall {
            projectApi.getProjectList(
                search = filters.search?.takeIf { it.isNotBlank() },
                kategori = filters.kategoriId,
                kategoriId = filters.kategoriId,
                minBudget = filters.minBudget,
                maxBudget = filters.maxBudget,
                includeRelatedSkills = filters.includeRelatedSkills.takeIf { it },
                page = filters.page,
                limit = filters.limit
            ).data.map { it.toDomain() }
        }

    override suspend fun getMyProjectList(filters: ProjectListFilters): Resource<List<Project>> =
        safeApiCall {
            projectApi.getMyProjectList(
                search = filters.search?.takeIf { it.isNotBlank() },
                kategori = filters.kategoriId,
                kategoriId = filters.kategoriId,
                minBudget = filters.minBudget,
                maxBudget = filters.maxBudget,
                includeRelatedSkills = filters.includeRelatedSkills.takeIf { it },
                page = filters.page,
                limit = filters.limit
            ).data.map { it.toDomain() }
        }

    override suspend fun getProjectDetail(id: String): Resource<Project> =
        safeApiCall {
            projectApi.getProjectDetail(id).toDomain()
        }

    override suspend fun createProject(params: CreateProjectParams): Resource<Project> =
        safeApiCall {
            if (params.image != null) {
                val request = params.toMultipartRequest()
                projectApi.createProjectMultipart(
                    kategoriId = request.kategoriId,
                    title = request.title,
                    description = request.description,
                    budget = request.budget,
                    deadline = request.deadline,
                    image = request.image
                ).toDomain()
            } else {
                projectApi.createProject(params.toRequest()).toDomain()
            }
        }

    override suspend fun takeProject(id: String): Resource<Pesanan> =
        safeApiCall {
            projectApi.takeProject(id).toDomain()
        }

    override suspend fun updateProject(id: String, params: UpdateProjectParams): Resource<Project> =
        safeApiCall {
            if (params.image != null) {
                val (fields, imagePart) = params.toMultipartRequest()
                projectApi.updateProjectMultipart(
                    id = id,
                    title = fields.title,
                    description = fields.description,
                    budget = fields.budget,
                    deadline = fields.deadline,
                    kategoriId = fields.kategoriId,
                    status = fields.status,
                    image = imagePart
                ).toDomain()
            } else {
                projectApi.updateProject(id, params.toRequest()).toDomain()
            }
        }

    override suspend fun deleteProject(id: String): Resource<Unit> =
        safeApiCall {
            projectApi.deleteProject(id)
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
