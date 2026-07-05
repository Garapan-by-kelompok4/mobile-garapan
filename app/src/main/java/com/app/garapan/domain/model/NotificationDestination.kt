package com.app.garapan.domain.model

sealed interface NotificationDestination {
    data class OrderDetail(val pesananId: String) : NotificationDestination
    data class AllReviews(val jasaId: String) : NotificationDestination
    data class Chat(val conversationId: String?) : NotificationDestination
    data class ProjectDetail(val projectId: String) : NotificationDestination
    data object OrderHistory : NotificationDestination
    data object NotificationsList : NotificationDestination
}

fun resolveNotificationDestination(
    type: NotificationType,
    meta: NotificationMeta?
): NotificationDestination {
    val jasaId = meta?.jasaId
    if (!jasaId.isNullOrBlank() && type == NotificationType.REVIEW_RECEIVED) {
        return NotificationDestination.AllReviews(jasaId)
    }
    if (type == NotificationType.CHAT_MESSAGE) {
        return NotificationDestination.Chat(meta?.conversationId)
    }
    val projectId = meta?.projectId
    if (
        !projectId.isNullOrBlank() &&
        (
            type == NotificationType.PROPOSAL_RECEIVED ||
                type == NotificationType.PROPOSAL_REJECTED
            )
    ) {
        return NotificationDestination.ProjectDetail(projectId)
    }
    val pesananId = meta?.pesananId
    if (!pesananId.isNullOrBlank()) {
        return NotificationDestination.OrderDetail(pesananId)
    }
    return when (type) {
        NotificationType.REVIEW_RECEIVED,
        NotificationType.PROPOSAL_RECEIVED,
        NotificationType.PROPOSAL_REJECTED -> NotificationDestination.NotificationsList
        else -> NotificationDestination.OrderHistory
    }
}
