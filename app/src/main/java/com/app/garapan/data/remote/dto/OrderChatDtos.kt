package com.app.garapan.data.remote.dto

/** One conversation row from `GET /chat`. */
data class ConversationDto(
    val conversationId: String,
    val counterparty: ConversationCounterpartyDto? = null,
    val lastMessage: OrderChatMessageDto? = null,
    val unreadCount: Int? = null,
    val activeOrder: ActiveOrderDto? = null
)

data class ConversationCounterpartyDto(
    val id: String? = null,
    val email: String? = null,
    val role: String? = null,
    val displayName: String? = null,
    val companyName: String? = null,
    val university: String? = null,
    val avatarUrl: String? = null
)

data class ActiveOrderDto(
    val pesananId: String,
    val status: String,
    val title: String? = null
)

data class OpenConversationRequestDto(
    val counterpartyId: String
)

data class OpenConversationResponseDto(
    val conversationId: String,
    val counterparty: ConversationCounterpartyDto? = null,
    val activeOrder: ActiveOrderDto? = null
)

/** A single chat message from `GET /chat/{conversationId}/messages` or `POST`. */
data class OrderChatMessageDto(
    val id: String? = null,
    val conversationId: String? = null,
    val pesananId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val messageType: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val readAt: String? = null,
    val createdAt: String? = null
)

/** Paginated message history page from `GET /chat/{conversationId}/messages` (ascending). */
data class OrderChatPageDto(
    val data: List<OrderChatMessageDto>? = null,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)

data class SendOrderMessageRequestDto(
    val message: String,
    val pesananId: String? = null
)
