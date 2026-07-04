package com.app.garapan.presentation.util

import java.util.Locale

object CurrencyFormatter {
    fun formatRupiah(amount: Double): String {
        val formatted = String.format(Locale("id", "ID"), "%,.0f", amount).replace(',', '.')
        return "Rp $formatted"
    }

    fun formatRupiahRange(minAmount: Double?, maxAmount: Double?, fallbackAmount: Double): String {
        val min = minAmount?.takeIf { it > 0.0 }
        val max = maxAmount?.takeIf { it > 0.0 } ?: fallbackAmount.takeIf { it > 0.0 }

        return when {
            min != null && max != null && min != max -> "${formatRupiah(min)} - ${formatRupiah(max)}"
            max != null -> formatRupiah(max)
            min != null -> formatRupiah(min)
            else -> formatRupiah(fallbackAmount)
        }
    }
}
