package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toMultipartRequest
import com.app.garapan.data.mapper.toRequest
import com.app.garapan.data.remote.api.JasaApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateJasaParams
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.model.UpdateJasaParams
import com.app.garapan.domain.repository.JasaRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class JasaRepositoryImpl @Inject constructor(
    private val jasaApi: JasaApi
) : JasaRepository {

    override suspend fun getJasaList(filters: JasaListFilters): Resource<List<Jasa>> =
        safeApiCall {
            jasaApi.getJasaList(
                search = filters.search?.takeIf { it.isNotBlank() },
                kategoriId = filters.kategoriId,
                mahasiswaId = filters.mahasiswaId,
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                sort = filters.sort,
                includeRelatedSkills = filters.includeRelatedSkills.takeIf { it },
                page = filters.page,
                limit = filters.limit
            ).data.map { it.toDomain() }
        }

    override suspend fun getMyJasaList(filters: JasaListFilters): Resource<List<Jasa>> =
        safeApiCall {
            jasaApi.getMyJasaList(
                search = filters.search?.takeIf { it.isNotBlank() },
                kategoriId = filters.kategoriId,
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                sort = filters.sort,
                page = filters.page,
                limit = filters.limit
            ).data.map { it.toDomain() }
        }

    override suspend fun getJasaDetail(id: String): Resource<Jasa> =
        safeApiCall {
            jasaApi.getJasaDetail(id).toDomain()
        }

    override suspend fun createJasa(params: CreateJasaParams): Resource<Jasa> =
        safeApiCall {
            val request = params.toMultipartRequest()
            jasaApi.createJasa(
                kategoriId = request.kategoriId,
                title = request.title,
                description = request.description,
                price = request.price,
                image = request.image
            ).toDomain()
        }

    override suspend fun updateJasa(id: String, params: UpdateJasaParams): Resource<Jasa> =
        safeApiCall {
            jasaApi.updateJasa(id, params.toRequest()).toDomain()
        }

    override suspend fun deleteJasa(id: String): Resource<Unit> =
        safeApiCall {
            jasaApi.deleteJasa(id)
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
