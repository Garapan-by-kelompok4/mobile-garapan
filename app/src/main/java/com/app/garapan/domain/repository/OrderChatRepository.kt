package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ChatAttachmentUpload
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.OpenConversationResult
import com.app.garapan.domain.model.OrderChatMessage
import com.app.garapan.domain.model.OrderChatPage

interface OrderChatRepository {
    suspend fun getConversations(): Resource<List<Conversation>>
    suspend fun openConversation(counterpartyId: String): Resource<OpenConversationResult>
    suspend fun getMessages(conversationId: String, page: Int, limit: Int): Resource<OrderChatPage>
    suspend fun sendMessage(
        conversationId: String,
        message: String,
        pesananId: String? = null
    ): Resource<OrderChatMessage>
    suspend fun sendAttachment(
        conversationId: String,
        attachment: ChatAttachmentUpload,
        pesananId: String? = null
    ): Resource<OrderChatMessage>
    suspend fun markRead(conversationId: String): Resource<Unit>
}
