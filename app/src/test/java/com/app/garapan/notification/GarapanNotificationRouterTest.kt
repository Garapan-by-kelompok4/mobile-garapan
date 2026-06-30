package com.app.garapan.notification

import com.app.garapan.presentation.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GarapanNotificationRouterTest {

    @Test
    fun chatMessageWithConversationIdRoutesToPeerChat() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "CHAT_MESSAGE",
            conversationId = "conv-123"
        )

        assertEquals(Routes.chatRoute("conv-123"), route)
    }

    @Test
    fun chatMessageWithoutConversationIdRoutesToSupportChat() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "CHAT_MESSAGE",
            conversationId = null
        )

        assertEquals(Routes.supportChatRoute(), route)
    }

    @Test
    fun chatMessageWithBlankConversationIdRoutesToSupportChat() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "CHAT_MESSAGE",
            conversationId = ""
        )

        assertEquals(Routes.supportChatRoute(), route)
    }

    @Test
    fun missingTypeDoesNotRoute() {
        val route = GarapanNotificationRouter.routeFromData(
            type = null,
            conversationId = "conv-123"
        )

        assertNull(route)
    }
}
