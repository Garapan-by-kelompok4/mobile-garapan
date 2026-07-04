package com.app.garapan.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDestinationResolverTest {

    @Test
    fun reviewReceivedPrefersAllReviewsWhenJasaIdPresent() {
        val destination = resolveNotificationDestination(
            type = NotificationType.REVIEW_RECEIVED,
            meta = NotificationMeta(jasaId = "jasa-1", pesananId = "pesanan-1")
        )

        assertTrue(destination is NotificationDestination.AllReviews)
        assertEquals("jasa-1", (destination as NotificationDestination.AllReviews).jasaId)
    }

    @Test
    fun reviewReceivedFallsBackToOrderDetailWhenOnlyPesananIdPresent() {
        val destination = resolveNotificationDestination(
            type = NotificationType.REVIEW_RECEIVED,
            meta = NotificationMeta(pesananId = "pesanan-1")
        )

        assertTrue(destination is NotificationDestination.OrderDetail)
    }

    @Test
    fun chatMessageRoutesToChatDestination() {
        val destination = resolveNotificationDestination(
            type = NotificationType.CHAT_MESSAGE,
            meta = NotificationMeta(conversationId = "conv-1")
        )

        assertTrue(destination is NotificationDestination.Chat)
        assertEquals("conv-1", (destination as NotificationDestination.Chat).conversationId)
    }

    @Test
    fun proposalReceivedRoutesToProjectDetail() {
        val destination = resolveNotificationDestination(
            type = NotificationType.PROPOSAL_RECEIVED,
            meta = NotificationMeta(projectId = "project-1")
        )

        assertTrue(destination is NotificationDestination.ProjectDetail)
    }

    @Test
    fun orderPaidRoutesToOrderDetail() {
        val destination = resolveNotificationDestination(
            type = NotificationType.ORDER_PAID,
            meta = NotificationMeta(pesananId = "pesanan-1")
        )

        assertTrue(destination is NotificationDestination.OrderDetail)
    }
}
