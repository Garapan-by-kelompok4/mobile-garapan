package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.KategoriApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Kategori
import com.app.garapan.domain.repository.KategoriRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class KategoriRepositoryImpl @Inject constructor(
    private val kategoriApi: KategoriApi
) : KategoriRepository {

    override suspend fun getKategoriList(): Resource<List<Kategori>> =
        safeApiCall {
            kategoriApi.getKategoriList().value.map { it.toDomain() }
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
