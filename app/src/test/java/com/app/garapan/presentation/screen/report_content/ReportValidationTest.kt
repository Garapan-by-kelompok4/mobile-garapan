package com.app.garapan.presentation.screen.report_content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReportValidationTest {

    @Test
    fun rejectsBlankReason() {
        assertEquals(
            "Alasan laporan tidak boleh kosong.",
            ReportValidation.validateReason("   ")
        )
    }

    @Test
    fun rejectsTooShortReason() {
        assertEquals(
            "Alasan laporan minimal 10 karakter.",
            ReportValidation.validateReason("pendek")
        )
    }

    @Test
    fun rejectsTooLongReason() {
        val longReason = "a".repeat(ReportValidation.MAX_REASON_LENGTH + 1)
        assertEquals(
            "Alasan laporan maksimal 1000 karakter.",
            ReportValidation.validateReason(longReason)
        )
    }

    @Test
    fun acceptsValidReason() {
        assertNull(
            ReportValidation.validateReason("Konten ini terlihat menyesatkan dan tidak sesuai aturan.")
        )
    }

    @Test
    fun trimsReasonBeforeValidation() {
        assertNull(
            ReportValidation.validateReason("  ${"x".repeat(10)}  ")
        )
    }
}
