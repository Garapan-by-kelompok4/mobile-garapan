package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toSupportMessage
import com.app.garapan.data.mapper.toSupportThreadPage
import com.app.garapan.data.remote.api.SupportChatApi
import com.app.garapan.data.remote.dto.SendSupportMessageRequestDto
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.SupportMessage
import com.app.garapan.domain.model.SupportThreadPage
import com.app.garapan.domain.repository.SupportChatRepository
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SupportChatRepositoryImpl @Inject constructor(
    private val supportChatApi: SupportChatApi,
    private val gson: Gson
) : SupportChatRepository {

    override suspend fun getMySupportThread(page: Int, limit: Int): Resource<SupportThreadPage> =
        safeApiCall {
            supportChatApi.getMySupportThread(page, limit).toSupportThreadPage(gson, page, limit)
        }

    override suspend fun sendMessage(message: String): Resource<SupportMessage> =
        safeApiCall {
            supportChatApi.sendMessage(SendSupportMessageRequestDto(message))
                .toSupportMessage(gson, fallbackMessage = message)
        }

    override suspend fun markMyThreadRead(): Resource<Unit> =
        safeApiCall {
            supportChatApi.markMyThreadRead()
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
