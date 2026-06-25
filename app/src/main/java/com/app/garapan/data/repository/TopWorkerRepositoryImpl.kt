package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.TopWorkerApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.TopWorker
import com.app.garapan.domain.repository.TopWorkerRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class TopWorkerRepositoryImpl @Inject constructor(
    private val topWorkerApi: TopWorkerApi
) : TopWorkerRepository {

    override suspend fun getTopWorkers(): Resource<List<TopWorker>> =
        safeApiCall {
            topWorkerApi.getTopWorkers().map { it.toDomain() }
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
