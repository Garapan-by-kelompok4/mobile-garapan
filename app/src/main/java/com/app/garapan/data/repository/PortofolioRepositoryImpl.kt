package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toMultipartRequest
import com.app.garapan.data.remote.api.PortofolioApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePortofolioParams
import com.app.garapan.domain.model.Portofolio
import com.app.garapan.domain.repository.PortofolioRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class PortofolioRepositoryImpl @Inject constructor(
    private val portofolioApi: PortofolioApi
) : PortofolioRepository {

    override suspend fun getPortofolioList(mahasiswaId: String): Resource<List<Portofolio>> =
        safeApiCall {
            portofolioApi.getPortofolioList(mahasiswaId).map { it.toDomain() }
        }

    override suspend fun addPortofolio(params: CreatePortofolioParams): Resource<Portofolio> =
        safeApiCall {
            val request = params.toMultipartRequest()
            portofolioApi.createPortofolio(
                title = request.title,
                description = request.description,
                image = request.image,
                projectUrl = request.projectUrl
            ).toDomain()
        }

    override suspend fun deletePortofolio(id: String): Resource<Unit> =
        safeApiCall {
            portofolioApi.deletePortofolio(id)
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
