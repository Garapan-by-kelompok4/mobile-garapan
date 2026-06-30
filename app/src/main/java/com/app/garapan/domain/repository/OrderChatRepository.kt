package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.OrderChatMessage
import com.app.garapan.domain.model.OrderChatPage

interface OrderChatRepository {
    suspend fun getConversations(): Resource<List<Conversation>>
    suspend fun getMessages(pesananId: String, page: Int, limit: Int): Resource<OrderChatPage>
    suspend fun sendMessage(pesananId: String, message: String): Resource<OrderChatMessage>
    suspend fun markRead(pesananId: String): Resource<Unit>
}
