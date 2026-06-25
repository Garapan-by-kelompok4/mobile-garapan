package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.ProjectApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
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

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
