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
    val pesananId: String? = null,
    val reviewId: String? = null,
    val jasaId: String? = null
)

enum class NotificationType {
    ORDER_PAID,
    ORDER_DELIVERED,
    ORDER_COMPLETED,
    PROJECT_CLAIMED,
    REVIEW_RECEIVED,
    CHAT_MESSAGE;

    companion object {
        fun fromApiValue(value: String): NotificationType =
            entries.firstOrNull { it.name == value.uppercase() } ?: ORDER_PAID
    }
}
