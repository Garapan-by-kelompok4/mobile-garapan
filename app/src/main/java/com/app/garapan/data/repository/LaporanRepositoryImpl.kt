package com.app.garapan.data.repository

import com.app.garapan.data.remote.api.LaporanApi
import com.app.garapan.data.remote.dto.CreateLaporanDto
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.repository.LaporanRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class LaporanRepositoryImpl @Inject constructor(
    private val laporanApi: LaporanApi
) : LaporanRepository {

    override suspend fun createLaporan(pesananId: String, reason: String): Resource<Unit> =
        safeApiCall {
            laporanApi.createLaporan(CreateLaporanDto(pesananId = pesananId, reason = reason))
            Unit
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
