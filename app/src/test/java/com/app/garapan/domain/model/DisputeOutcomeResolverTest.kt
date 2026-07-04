package com.app.garapan.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisputeOutcomeResolverTest {

    @Test
    fun returnsNullForPendingLaporan() {
        assertNull(
            DisputeOutcomeResolver.resolveOutcome(
                laporanStatus = LaporanStatus.PENDING,
                orderStatus = PesananStatus.DISPUTED,
                refundAmount = null
            )
        )
    }

    @Test
    fun resolvesRejectOutcome() {
        assertEquals(
            DisputeOutcome.REJECT,
            DisputeOutcomeResolver.resolveOutcome(
                laporanStatus = LaporanStatus.REJECTED,
                orderStatus = PesananStatus.DELIVERED,
                refundAmount = null
            )
        )
    }

    @Test
    fun resolvesRefundOutcome() {
        assertEquals(
            DisputeOutcome.REFUND,
            DisputeOutcomeResolver.resolveOutcome(
                laporanStatus = LaporanStatus.RESOLVED,
                orderStatus = PesananStatus.CANCELLED,
                refundAmount = null
            )
        )
    }

    @Test
    fun resolvesPartialRefundOutcome() {
        assertEquals(
            DisputeOutcome.PARTIAL_REFUND,
            DisputeOutcomeResolver.resolveOutcome(
                laporanStatus = LaporanStatus.RESOLVED,
                orderStatus = PesananStatus.COMPLETED,
                refundAmount = 50_000.0
            )
        )
    }

    @Test
    fun resolvesReleaseOutcome() {
        assertEquals(
            DisputeOutcome.RELEASE,
            DisputeOutcomeResolver.resolveOutcome(
                laporanStatus = LaporanStatus.RESOLVED,
                orderStatus = PesananStatus.COMPLETED,
                refundAmount = null
            )
        )
    }
}
