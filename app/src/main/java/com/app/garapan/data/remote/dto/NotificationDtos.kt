package com.app.garapan.data.remote.dto

data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val read: Boolean,
    val createdAt: String,
    val meta: NotificationMetaDto? = null
)

data class NotificationMetaDto(
    val conversationId: String? = null,
    val pesananId: String? = null,
    val reviewId: String? = null,
    val jasaId: String? = null,
    val senderId: String? = null
)

data class NotificationListResponseDto(
    val data: List<NotificationDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class MarkAllNotificationsReadResponseDto(
    val updated: Int
)
