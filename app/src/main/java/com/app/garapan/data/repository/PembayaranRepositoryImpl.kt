package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toRequest
import com.app.garapan.data.remote.api.PembayaranApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.model.Pembayaran
import com.app.garapan.domain.repository.PembayaranRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class PembayaranRepositoryImpl @Inject constructor(
    private val pembayaranApi: PembayaranApi
) : PembayaranRepository {

    override suspend fun createPaymentToken(params: CreatePaymentTokenParams): Resource<Pembayaran> =
        safeApiCall { pembayaranApi.createPaymentToken(params.toRequest()).toDomain() }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
