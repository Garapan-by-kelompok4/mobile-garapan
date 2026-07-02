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

    @Test
    fun orderTypesRouteToOrderDetail() {
        listOf(
            "ORDER_PAID",
            "ORDER_DELIVERED",
            "ORDER_COMPLETED",
            "ORDER_CANCELLED",
            "PROPOSAL_ACCEPTED",
            "DISPUTE_CREATED",
            "DISPUTE_RESOLVED"
        ).forEach { type ->
            val route = GarapanNotificationRouter.routeFromData(
                type = type,
                pesananId = "pesanan-1"
            )

            assertEquals(Routes.orderDetailRoute("pesanan-1"), route)
        }
    }

    @Test
    fun orderTypeWithoutPesananIdFallsBackToNotifications() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "ORDER_PAID",
            pesananId = ""
        )

        assertEquals(Routes.NOTIFICATIONS, route)
    }

    @Test
    fun proposalReceivedRoutesToProjectDetail() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "PROPOSAL_RECEIVED",
            projectId = "project-9"
        )

        assertEquals(Routes.projectDetailRoute("project-9"), route)
    }

    @Test
    fun proposalRejectedRoutesToProjectDetail() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "PROPOSAL_REJECTED",
            projectId = "project-9"
        )

        assertEquals(Routes.projectDetailRoute("project-9"), route)
    }

    @Test
    fun proposalReceivedWithoutProjectIdFallsBackToNotifications() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "PROPOSAL_RECEIVED",
            projectId = null
        )

        assertEquals(Routes.NOTIFICATIONS, route)
    }

    @Test
    fun unknownTypeRoutesToNotifications() {
        val route = GarapanNotificationRouter.routeFromData(
            type = "SOME_FUTURE_TYPE"
        )

        assertEquals(Routes.NOTIFICATIONS, route)
    }
}
