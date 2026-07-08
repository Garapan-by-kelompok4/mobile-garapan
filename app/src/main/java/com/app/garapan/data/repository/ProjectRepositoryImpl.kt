package com.app.garapan.data.repository

import com.app.garapan.data.mapper.submitProposalRequest
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
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.TakenProject
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
                sort = filters.sort,
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
                sort = filters.sort,
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
                    minBudget = request.minBudget,
                    maxBudget = request.maxBudget,
                    deadline = request.deadline,
                    image = request.image
                ).toDomain()
            } else {
                projectApi.createProject(params.toRequest()).toDomain()
            }
        }

    override suspend fun submitProposal(
        projectId: String,
        message: String,
        proposedPrice: Double
    ): Resource<ProjectProposal> =
        safeApiCall {
            projectApi.submitProposal(projectId, submitProposalRequest(message, proposedPrice)).toDomain()
        }

    override suspend fun withdrawProposal(projectId: String): Resource<Unit> =
        safeApiCall {
            projectApi.withdrawProposal(projectId)
        }

    override suspend fun getProjectProposals(projectId: String): Resource<List<ProjectProposal>> =
        safeApiCall {
            projectApi.getProjectProposals(projectId).map { it.toDomain() }
        }

    override suspend fun getMyProposals(page: Int, limit: Int): Resource<List<ProjectProposal>> =
        safeApiCall {
            projectApi.getMyProposals(page, limit).data.map { it.toDomain() }
        }

    override suspend fun getMyTakenProjects(page: Int, limit: Int): Resource<List<TakenProject>> =
        safeApiCall {
            projectApi.getMyTakenProjects(page, limit).data.map { it.toDomain() }
        }

    override suspend fun acceptProposal(projectId: String, proposalId: String): Resource<Pesanan> =
        safeApiCall {
            projectApi.acceptProposal(projectId, proposalId).toDomain()
        }

    override suspend fun rejectProposal(projectId: String, proposalId: String): Resource<Unit> =
        safeApiCall {
            projectApi.rejectProposal(projectId, proposalId)
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
                    minBudget = fields.minBudget,
                    maxBudget = fields.maxBudget,
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
