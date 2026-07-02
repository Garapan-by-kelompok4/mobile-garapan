package com.app.garapan.domain.model

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val read: Boolean,
    val createdAt: String,
    val meta: NotificationMeta? = null
)

data class NotificationMeta(
    val conversationId: String? = null,
    val pesananId: String? = null,
    val reviewId: String? = null,
    val jasaId: String? = null,
    val senderId: String? = null,
    val projectId: String? = null,
    val proposalId: String? = null,
    val laporanId: String? = null
)

enum class NotificationType {
    ORDER_PAID,
    ORDER_DELIVERED,
    ORDER_COMPLETED,
    ORDER_CANCELLED,
    PROJECT_CLAIMED,
    REVIEW_RECEIVED,
    CHAT_MESSAGE,
    PROPOSAL_RECEIVED,
    PROPOSAL_ACCEPTED,
    PROPOSAL_REJECTED,
    DISPUTE_CREATED,
    DISPUTE_RESOLVED,
    UNKNOWN;

    companion object {
        fun fromApiValue(value: String): NotificationType =
            entries.firstOrNull { it.name == value.uppercase() } ?: UNKNOWN
    }
}
