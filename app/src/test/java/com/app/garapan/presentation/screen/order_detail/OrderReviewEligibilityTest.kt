package com.app.garapan.presentation.screen.order_detail

import com.app.garapan.domain.model.PesananStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderReviewEligibilityTest {

    @Test
    fun allowsCompletedProjectOrderForBuyer() {
        assertTrue(
            OrderReviewEligibility.canReview(
                status = PesananStatus.COMPLETED,
                isBuyer = true
            )
        )
    }

    @Test
    fun blocksCompletedOrderForProvider() {
        assertFalse(
            OrderReviewEligibility.canReview(
                status = PesananStatus.COMPLETED,
                isBuyer = false
            )
        )
    }

    @Test
    fun blocksDeliveredOrderForBuyerUntilCompleted() {
        assertFalse(
            OrderReviewEligibility.canReview(
                status = PesananStatus.DELIVERED,
                isBuyer = true
            )
        )
    }
}
