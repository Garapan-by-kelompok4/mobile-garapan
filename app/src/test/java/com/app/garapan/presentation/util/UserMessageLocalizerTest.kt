package com.app.garapan.presentation.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserMessageLocalizerTest {

    @Test
    fun detectsOrderAlreadyReviewedMessage() {
        assertTrue(UserMessageLocalizer.isOrderAlreadyReviewed("Order has already been reviewed"))
    }

    @Test
    fun ignoresUnrelatedErrorMessages() {
        assertFalse(UserMessageLocalizer.isOrderAlreadyReviewed("Jasa not found"))
    }
}
