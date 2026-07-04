package com.app.garapan.notification

import android.content.Intent
import com.app.garapan.domain.model.NotificationDestination
import com.app.garapan.domain.model.NotificationMeta
import com.app.garapan.domain.model.NotificationType
import com.app.garapan.domain.model.resolveNotificationDestination
import com.app.garapan.presentation.navigation.Routes

object GarapanNotificationRouter {
    fun routeFromIntent(intent: Intent?): String? {
        val type = intent?.getStringExtra(KEY_TYPE) ?: return null
        return routeFromData(
            type = type,
            conversationId = intent.getStringExtra(KEY_CONVERSATION_ID),
            pesananId = intent.getStringExtra(KEY_PESANAN_ID),
            projectId = intent.getStringExtra(KEY_PROJECT_ID),
            jasaId = intent.getStringExtra(KEY_JASA_ID)
        )
    }

    fun notificationIdFromIntent(intent: Intent?): String? =
        intent?.getStringExtra(KEY_NOTIFICATION_ID)?.takeIf { it.isNotBlank() }

    internal fun routeFromData(
        type: String?,
        conversationId: String? = null,
        pesananId: String? = null,
        projectId: String? = null,
        jasaId: String? = null
    ): String? {
        if (type.isNullOrBlank()) return null
        val destination = resolveNotificationDestination(
            type = NotificationType.fromApiValue(type),
            meta = NotificationMeta(
                conversationId = conversationId,
                pesananId = pesananId,
                projectId = projectId,
                jasaId = jasaId
            )
        )
        return destination.toRoute()
    }

    private fun NotificationDestination.toRoute(): String = when (this) {
        is NotificationDestination.Chat -> {
            conversationId?.takeIf { it.isNotBlank() }?.let { Routes.chatRoute(it) }
                ?: Routes.supportChatRoute()
        }
        is NotificationDestination.OrderDetail -> Routes.orderDetailRoute(pesananId)
        is NotificationDestination.AllReviews -> Routes.allReviewsRoute(jasaId)
        is NotificationDestination.ProjectDetail -> Routes.projectDetailRoute(projectId)
        NotificationDestination.OrderHistory -> Routes.ORDER_HISTORY
        NotificationDestination.NotificationsList -> Routes.NOTIFICATIONS
    }

    const val KEY_TYPE = "type"
    const val KEY_CONVERSATION_ID = "conversationId"
    const val KEY_PESANAN_ID = "pesananId"
    const val KEY_PROJECT_ID = "projectId"
    const val KEY_JASA_ID = "jasaId"
    const val KEY_NOTIFICATION_ID = "notificationId"
}
