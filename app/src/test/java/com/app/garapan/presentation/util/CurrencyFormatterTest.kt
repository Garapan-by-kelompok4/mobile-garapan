package com.app.garapan.presentation.util

import java.math.BigDecimal
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

    @Test
    fun `parses rupiah input with Indonesian thousands separators`() {
        assertEquals(
            BigDecimal("8200000"),
            CurrencyFormatter.parseRupiahAmount("Rp 8.200.000")
        )
    }

    @Test
    fun `parses rupiah input with decimal comma`() {
        assertEquals(
            BigDecimal("8200000.50"),
            CurrencyFormatter.parseRupiahAmount("8.200.000,50")
        )
    }

    @Test
    fun `rejects input with too many decimal digits`() {
        assertEquals(null, CurrencyFormatter.parseRupiahAmount("8.200.000,505"))
    }

    @Test
    fun `formats raw rupiah input with Indonesian thousands separators`() {
        assertEquals("200.000", CurrencyFormatter.formatRupiahInput("200000"))
        assertEquals("2.000.000", CurrencyFormatter.formatRupiahInput("2000000"))
    }

    @Test
    fun `keeps formatted rupiah input stable`() {
        assertEquals("2.000.000", CurrencyFormatter.formatRupiahInput("Rp 2.000.000"))
    }

    @Test
    fun `formats rupiah input while preserving decimal comma`() {
        assertEquals("2.000.000,50", CurrencyFormatter.formatRupiahInput("2000000,50"))
    }
}
