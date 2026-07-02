package com.app.garapan.notification

import android.content.Intent
import com.app.garapan.presentation.navigation.Routes

object GarapanNotificationRouter {
    fun routeFromIntent(intent: Intent?): String? {
        val type = intent?.getStringExtra(KEY_TYPE)
        val conversationId = intent?.getStringExtra(KEY_CONVERSATION_ID)
        val pesananId = intent?.getStringExtra(KEY_PESANAN_ID)
        val projectId = intent?.getStringExtra(KEY_PROJECT_ID)

        return routeFromData(
            type = type,
            conversationId = conversationId,
            pesananId = pesananId,
            projectId = projectId
        )
    }

    internal fun routeFromData(
        type: String?,
        conversationId: String? = null,
        pesananId: String? = null,
        projectId: String? = null
    ): String? {
        if (type.isNullOrBlank()) return null
        return when (type) {
            TYPE_CHAT_MESSAGE -> {
                conversationId?.takeIf { it.isNotBlank() }?.let { Routes.chatRoute(it) }
                    ?: Routes.supportChatRoute()
            }
            TYPE_ORDER_PAID,
            TYPE_ORDER_DELIVERED,
            TYPE_ORDER_COMPLETED,
            TYPE_ORDER_CANCELLED,
            TYPE_PROJECT_CLAIMED,
            TYPE_PROPOSAL_ACCEPTED,
            TYPE_DISPUTE_CREATED,
            TYPE_DISPUTE_RESOLVED,
            TYPE_REVIEW_RECEIVED -> pesananId?.takeIf { it.isNotBlank() }?.let {
                Routes.orderDetailRoute(it)
            } ?: Routes.NOTIFICATIONS
            TYPE_PROPOSAL_RECEIVED,
            TYPE_PROPOSAL_REJECTED -> projectId?.takeIf { it.isNotBlank() }?.let {
                Routes.projectDetailRoute(it)
            } ?: Routes.NOTIFICATIONS
            else -> Routes.NOTIFICATIONS
        }
    }

    private const val KEY_TYPE = "type"
    private const val KEY_CONVERSATION_ID = "conversationId"
    private const val KEY_PESANAN_ID = "pesananId"
    private const val KEY_PROJECT_ID = "projectId"

    private const val TYPE_CHAT_MESSAGE = "CHAT_MESSAGE"
    private const val TYPE_ORDER_PAID = "ORDER_PAID"
    private const val TYPE_ORDER_DELIVERED = "ORDER_DELIVERED"
    private const val TYPE_ORDER_COMPLETED = "ORDER_COMPLETED"
    private const val TYPE_ORDER_CANCELLED = "ORDER_CANCELLED"
    private const val TYPE_PROJECT_CLAIMED = "PROJECT_CLAIMED"
    private const val TYPE_REVIEW_RECEIVED = "REVIEW_RECEIVED"
    private const val TYPE_PROPOSAL_RECEIVED = "PROPOSAL_RECEIVED"
    private const val TYPE_PROPOSAL_ACCEPTED = "PROPOSAL_ACCEPTED"
    private const val TYPE_PROPOSAL_REJECTED = "PROPOSAL_REJECTED"
    private const val TYPE_DISPUTE_CREATED = "DISPUTE_CREATED"
    private const val TYPE_DISPUTE_RESOLVED = "DISPUTE_RESOLVED"
}
