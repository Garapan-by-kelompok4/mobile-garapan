package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.NotificationDto
import com.app.garapan.data.remote.dto.NotificationMetaDto
import com.app.garapan.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationMappersTest {

    @Test
    fun knownTypesMapToMatchingEnumValues() {
        listOf(
            "ORDER_PAID" to NotificationType.ORDER_PAID,
            "ORDER_DELIVERED" to NotificationType.ORDER_DELIVERED,
            "ORDER_COMPLETED" to NotificationType.ORDER_COMPLETED,
            "ORDER_CANCELLED" to NotificationType.ORDER_CANCELLED,
            "PROJECT_CLAIMED" to NotificationType.PROJECT_CLAIMED,
            "REVIEW_RECEIVED" to NotificationType.REVIEW_RECEIVED,
            "CHAT_MESSAGE" to NotificationType.CHAT_MESSAGE,
            "PROPOSAL_RECEIVED" to NotificationType.PROPOSAL_RECEIVED,
            "PROPOSAL_ACCEPTED" to NotificationType.PROPOSAL_ACCEPTED,
            "PROPOSAL_REJECTED" to NotificationType.PROPOSAL_REJECTED,
            "DISPUTE_CREATED" to NotificationType.DISPUTE_CREATED,
            "DISPUTE_RESOLVED" to NotificationType.DISPUTE_RESOLVED
        ).forEach { (apiValue, expected) ->
            assertEquals(expected, NotificationType.fromApiValue(apiValue))
        }
    }

    @Test
    fun unknownTypeMapsToUnknownInsteadOfOrderPaid() {
        assertEquals(
            NotificationType.UNKNOWN,
            NotificationType.fromApiValue("SOME_FUTURE_TYPE")
        )
    }

    @Test
    fun metaKeepsProjectProposalAndLaporanIds() {
        val domain = NotificationDto(
            id = "n-1",
            title = "Proposal baru",
            body = "Ada proposal baru",
            type = "PROPOSAL_RECEIVED",
            read = false,
            createdAt = "2026-07-01T00:00:00Z",
            meta = NotificationMetaDto(
                projectId = "project-9",
                proposalId = "proposal-3",
                laporanId = "laporan-5"
            )
        ).toDomain()

        assertEquals("project-9", domain.meta?.projectId)
        assertEquals("proposal-3", domain.meta?.proposalId)
        assertEquals("laporan-5", domain.meta?.laporanId)
    }
}
