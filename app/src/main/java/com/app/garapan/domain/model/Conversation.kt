package com.app.garapan.domain.model

/**
 * One inbox conversation per counterparty. Pure data; the ViewModel maps
 * it to the UI preview item (initials, relative time, status chip).
 */
data class Conversation(
    val conversationId: String,
    val counterpartyId: String?,
    val counterpartyName: String,
    val lastMessage: String?,
    val lastMessageIsFile: Boolean,
    val lastMessageAt: String?,
    val unreadCount: Int,
    val activeOrder: ActiveOrder?
)

data class ActiveOrder(
    val pesananId: String,
    val status: PesananStatus,
    val title: String?
)

data class OpenConversationResult(
    val conversationId: String,
    val counterpartyName: String,
    val activeOrder: ActiveOrder?
)
