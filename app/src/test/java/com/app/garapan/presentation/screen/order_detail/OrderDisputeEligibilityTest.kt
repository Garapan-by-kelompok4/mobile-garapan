package com.app.garapan.presentation.screen.order_detail

import com.app.garapan.domain.model.PesananStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderDisputeEligibilityTest {

    @Test
    fun allowsInProgressOrderForParticipant() {
        assertTrue(
            OrderDisputeEligibility.canDispute(
                status = PesananStatus.IN_PROGRESS,
                isParticipant = true
            )
        )
    }

    @Test
    fun allowsDeliveredOrderForParticipant() {
        assertTrue(
            OrderDisputeEligibility.canDispute(
                status = PesananStatus.DELIVERED,
                isParticipant = true
            )
        )
    }

    @Test
    fun blocksDisputeForNonParticipant() {
        assertFalse(
            OrderDisputeEligibility.canDispute(
                status = PesananStatus.DELIVERED,
                isParticipant = false
            )
        )
    }

    @Test
    fun blocksDisputeWhenOrderAlreadyDisputed() {
        assertFalse(
            OrderDisputeEligibility.canDispute(
                status = PesananStatus.DISPUTED,
                isParticipant = true
            )
        )
    }

    @Test
    fun blocksDisputeForCompletedOrder() {
        assertFalse(
            OrderDisputeEligibility.canDispute(
                status = PesananStatus.COMPLETED,
                isParticipant = true
            )
        )
    }

    @Test
    fun showsBannerForDisputedOrder() {
        assertTrue(OrderDisputeEligibility.showDisputedInfoBanner(PesananStatus.DISPUTED))
    }

    @Test
    fun hidesBannerForNonDisputedOrder() {
        assertFalse(OrderDisputeEligibility.showDisputedInfoBanner(PesananStatus.DELIVERED))
    }
}
