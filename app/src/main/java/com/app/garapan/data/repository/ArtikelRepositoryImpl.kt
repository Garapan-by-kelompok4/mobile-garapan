package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.ArtikelApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Artikel
import com.app.garapan.domain.model.ArtikelRecommendation
import com.app.garapan.domain.repository.ArtikelRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ArtikelRepositoryImpl @Inject constructor(
    private val artikelApi: ArtikelApi
) : ArtikelRepository {

    override suspend fun getArtikelList(page: Int, limit: Int): Resource<List<Artikel>> =
        safeApiCall {
            artikelApi.getArtikelList(page = page, limit = limit).data.map { it.toDomain() }
        }

    override suspend fun getArtikelDetail(id: String): Resource<Artikel> =
        safeApiCall {
            artikelApi.getArtikelDetail(id).toDomain()
        }

    override suspend fun getArtikelRecommendations(id: String, limit: Int): Resource<List<ArtikelRecommendation>> =
        safeApiCall {
            artikelApi.getArtikelRecommendations(id = id, limit = limit).data.map { it.toDomain() }
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
