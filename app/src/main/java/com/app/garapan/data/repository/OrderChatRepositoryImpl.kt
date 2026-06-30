package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.ChatApi
import com.app.garapan.data.remote.dto.SendOrderMessageRequestDto
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.OrderChatMessage
import com.app.garapan.domain.model.OrderChatPage
import com.app.garapan.domain.repository.OrderChatRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class OrderChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi
) : OrderChatRepository {

    override suspend fun getConversations(): Resource<List<Conversation>> =
        safeApiCall { chatApi.getConversations().map { it.toDomain() } }

    override suspend fun getMessages(
        pesananId: String,
        page: Int,
        limit: Int
    ): Resource<OrderChatPage> =
        safeApiCall { chatApi.getMessages(pesananId, page, limit).toDomain(page, limit) }

    override suspend fun sendMessage(pesananId: String, message: String): Resource<OrderChatMessage> =
        safeApiCall {
            chatApi.sendMessage(pesananId, SendOrderMessageRequestDto(message)).toDomain()
        }

    override suspend fun markRead(pesananId: String): Resource<Unit> =
        safeApiCall {
            chatApi.markRead(pesananId)
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
