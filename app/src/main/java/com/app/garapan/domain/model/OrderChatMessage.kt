package com.app.garapan.domain.model

/** A single message in an order chat thread. */
data class OrderChatMessage(
    val id: String,
    val senderId: String?,
    val message: String,
    val isFile: Boolean,
    val fileName: String?,
    val fileUrl: String?,
    val createdAt: String?
)

/** One page of order-chat history (ascending, oldest -> newest). */
data class OrderChatPage(
    val messages: List<OrderChatMessage>,
    val total: Int,
    val page: Int,
    val limit: Int
)
