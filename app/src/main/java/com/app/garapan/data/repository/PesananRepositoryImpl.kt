package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toRequest
import com.app.garapan.data.remote.api.PesananApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePesananParams
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.repository.PesananRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class PesananRepositoryImpl @Inject constructor(
    private val pesananApi: PesananApi
) : PesananRepository {

    override suspend fun createPesanan(params: CreatePesananParams): Resource<Pesanan> =
        safeApiCall { pesananApi.createPesanan(params.toRequest()).toDomain() }

    override suspend fun getMyPesananList(page: Int, limit: Int): Resource<List<Pesanan>> =
        safeApiCall {
            pesananApi.getMyPesananList(page = page, limit = limit).data.map { it.toDomain() }
        }

    override suspend fun getPesananDetail(id: String): Resource<Pesanan> =
        safeApiCall { pesananApi.getPesananDetail(id).toDomain() }

    override suspend fun deliverPesanan(id: String): Resource<Pesanan> =
        safeApiCall { pesananApi.deliverPesanan(id).toDomain() }

    override suspend fun completePesanan(id: String): Resource<Pesanan> =
        safeApiCall { pesananApi.completePesanan(id).toDomain() }

    override suspend fun cancelPesanan(id: String): Resource<Pesanan> =
        safeApiCall { pesananApi.cancelPesanan(id).toDomain() }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
