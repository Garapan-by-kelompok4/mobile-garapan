package com.app.garapan.data.remote.dto

/** One conversation row from `GET /chat`. */
data class ConversationDto(
    val pesananId: String? = null,
    val status: String? = null,
    val counterparty: ConversationCounterpartyDto? = null,
    val lastMessage: OrderChatMessageDto? = null,
    val unreadCount: Int? = null
)

data class ConversationCounterpartyDto(
    val id: String? = null,
    val email: String? = null,
    val role: String? = null,
    val displayName: String? = null,
    val companyName: String? = null,
    val university: String? = null
)

/** A single order-chat message from `GET /chat/{id}/messages` or `POST /chat/{id}/messages`. */
data class OrderChatMessageDto(
    val id: String? = null,
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

/** Paginated message history page from `GET /chat/{id}/messages` (ascending). */
data class OrderChatPageDto(
    val data: List<OrderChatMessageDto>? = null,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)

data class SendOrderMessageRequestDto(
    val message: String
)
