package com.app.garapan.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `formats rupiah range when min and max differ`() {
        assertEquals(
            "Rp 1.000.000 - Rp 2.000.000",
            CurrencyFormatter.formatRupiahRange(
                minAmount = 1000000.0,
                maxAmount = 2000000.0,
                fallbackAmount = 2000000.0
            )
        )
    }

    @Test
    fun `formats single fallback budget for legacy project`() {
        assertEquals(
            "Rp 2.000.000",
            CurrencyFormatter.formatRupiahRange(
                minAmount = null,
                maxAmount = null,
                fallbackAmount = 2000000.0
            )
        )
    }

    @Test
    fun `formats single amount when range values are equal`() {
        assertEquals(
            "Rp 2.000.000",
            CurrencyFormatter.formatRupiahRange(
                minAmount = 2000000.0,
                maxAmount = 2000000.0,
                fallbackAmount = 2000000.0
            )
        )
    }
}
