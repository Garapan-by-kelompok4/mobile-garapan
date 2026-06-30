package com.app.garapan.notification

import com.app.garapan.presentation.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GarapanNotificationRouterTest {

    @Test
    fun chatMessageWithPesananIdRoutesToOrderChat() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "CHAT_MESSAGE",
            pesananId = "pesanan-123"
        )

        assertEquals(
            Routes.chatRoute("pesanan-123", source = Routes.CHAT_SOURCE_ORDER),
            route
        )
    }

    @Test
    fun chatMessageWithBlankPesananIdRoutesToSupportChat() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "CHAT_MESSAGE",
            pesananId = ""
        )

        assertEquals(Routes.supportChatRoute(), route)
    }

    @Test
    fun chatMessageWithoutPesananIdRoutesToSupportChat() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "CHAT_MESSAGE",
            pesananId = null
        )

        assertEquals(Routes.supportChatRoute(), route)
    }

    @Test
    fun missingTypeDoesNotRoute() {
        val route = GarapanNotificationRouter.routeFromData(
            type = null,
            pesananId = "pesanan-123"
        )

        assertNull(route)
    }
}
