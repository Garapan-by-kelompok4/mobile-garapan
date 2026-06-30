package com.app.garapan.notification

import android.content.Intent
import com.app.garapan.presentation.navigation.Routes

object GarapanNotificationRouter {
    fun routeFromIntent(intent: Intent?): String? {
        val type = intent?.getStringExtra(KEY_TYPE)
        val pesananId = intent?.getStringExtra(KEY_PESANAN_ID)

        return routeFromData(type = type, pesananId = pesananId)
    }

    internal fun routeFromData(type: String?, pesananId: String?): String? {
        if (type.isNullOrBlank()) return null
        return when (type) {
            TYPE_CHAT_MESSAGE -> {
                if (pesananId.isNullOrBlank()) {
                    Routes.supportChatRoute()
                } else {
                    Routes.chatRoute(
                        workerId = pesananId,
                        source = Routes.CHAT_SOURCE_ORDER
                    )
                }
            }
            TYPE_ORDER_PAID,
            TYPE_ORDER_DELIVERED,
            TYPE_ORDER_COMPLETED,
            TYPE_PROJECT_CLAIMED,
            TYPE_REVIEW_RECEIVED -> pesananId?.takeIf { it.isNotBlank() }?.let {
                Routes.orderDetailRoute(it)
            }
            else -> Routes.NOTIFICATIONS
        }
    }

    private const val KEY_TYPE = "type"
    private const val KEY_PESANAN_ID = "pesananId"

    private const val TYPE_CHAT_MESSAGE = "CHAT_MESSAGE"
    private const val TYPE_ORDER_PAID = "ORDER_PAID"
    private const val TYPE_ORDER_DELIVERED = "ORDER_DELIVERED"
    private const val TYPE_ORDER_COMPLETED = "ORDER_COMPLETED"
    private const val TYPE_PROJECT_CLAIMED = "PROJECT_CLAIMED"
    private const val TYPE_REVIEW_RECEIVED = "REVIEW_RECEIVED"
}
