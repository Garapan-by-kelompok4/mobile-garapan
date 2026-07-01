package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.ChatApi
import com.app.garapan.data.remote.dto.OpenConversationRequestDto
import com.app.garapan.data.remote.dto.SendOrderMessageRequestDto
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ChatAttachmentUpload
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.OpenConversationResult
import com.app.garapan.domain.model.OrderChatMessage
import com.app.garapan.domain.model.OrderChatPage
import com.app.garapan.domain.repository.OrderChatRepository
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class OrderChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi
) : OrderChatRepository {

    override suspend fun getConversations(): Resource<List<Conversation>> =
        safeApiCall { chatApi.getConversations().map { it.toDomain() } }

    override suspend fun openConversation(counterpartyId: String): Resource<OpenConversationResult> =
        safeApiCall {
            chatApi.openConversation(OpenConversationRequestDto(counterpartyId)).toDomain()
        }

    override suspend fun getMessages(
        conversationId: String,
        page: Int,
        limit: Int
    ): Resource<OrderChatPage> =
        safeApiCall { chatApi.getMessages(conversationId, page, limit).toDomain(page, limit) }

    override suspend fun sendMessage(
        conversationId: String,
        message: String,
        pesananId: String?
    ): Resource<OrderChatMessage> =
        safeApiCall {
            chatApi.sendMessage(
                conversationId,
                SendOrderMessageRequestDto(message = message, pesananId = pesananId)
            ).toDomain()
        }

    override suspend fun sendAttachment(
        conversationId: String,
        attachment: ChatAttachmentUpload,
        pesananId: String?
    ): Resource<OrderChatMessage> =
        safeApiCall {
            val body = attachment.bytes.toRequestBody(attachment.mimeType.toMediaType())
            val part = MultipartBody.Part.createFormData("file", attachment.fileName, body)
            chatApi.sendAttachment(conversationId, part, pesananId).toDomain()
        }

    override suspend fun markRead(conversationId: String): Resource<Unit> =
        safeApiCall {
            chatApi.markRead(conversationId)
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
