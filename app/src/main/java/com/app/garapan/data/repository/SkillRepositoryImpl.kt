package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.SkillApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Skill
import com.app.garapan.domain.repository.SkillRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SkillRepositoryImpl @Inject constructor(
    private val skillApi: SkillApi
) : SkillRepository {

    override suspend fun getSkillList(): Resource<List<Skill>> =
        safeApiCall {
            skillApi.getSkillList().map { it.toDomain() }
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
