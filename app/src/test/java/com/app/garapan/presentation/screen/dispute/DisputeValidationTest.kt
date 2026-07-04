package com.app.garapan.presentation.screen.dispute

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisputeValidationTest {

    @Test
    fun rejectsBlankReason() {
        assertEquals(
            "Alasan dispute tidak boleh kosong.",
            DisputeValidation.validateReason("   ")
        )
    }

    @Test
    fun rejectsReasonShorterThanMinimum() {
        assertEquals(
            "Alasan dispute minimal 10 karakter.",
            DisputeValidation.validateReason("pendek")
        )
    }

    @Test
    fun rejectsReasonLongerThanMaximum() {
        val longReason = "a".repeat(DisputeValidation.MAX_REASON_LENGTH + 1)
        assertEquals(
            "Alasan dispute maksimal 1000 karakter.",
            DisputeValidation.validateReason(longReason)
        )
    }

    @Test
    fun acceptsValidReason() {
        assertNull(
            DisputeValidation.validateReason("Pekerjaan tidak sesuai brief yang disepakati.")
        )
    }

    @Test
    fun trimsReasonBeforeValidation() {
        assertNull(
            DisputeValidation.validateReason("  ${"x".repeat(10)}  ")
        )
    }
}
