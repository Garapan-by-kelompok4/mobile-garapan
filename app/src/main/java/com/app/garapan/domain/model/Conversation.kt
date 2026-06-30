package com.app.garapan.domain.model

/**
 * One inbox conversation for an order (pesanan). Pure data; the ViewModel maps
 * it to the UI preview item (initials, relative time, status chip).
 */
data class Conversation(
    val pesananId: String,
    val status: PesananStatus?,
    val counterpartyId: String?,
    val counterpartyName: String,
    val lastMessage: String?,
    val lastMessageIsFile: Boolean,
    val lastMessageAt: String?,
    val unreadCount: Int
)
