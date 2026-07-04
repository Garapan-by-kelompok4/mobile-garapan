package com.app.garapan.presentation.util

import java.math.BigDecimal
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

    fun parseRupiahAmount(value: String): BigDecimal? {
        val compact = value
            .trim()
            .replace(Regex("(?i)rp"), "")
            .replace(Regex("\\s+"), "")

        if (compact.isBlank() || !compact.all { it.isDigit() || it == '.' || it == ',' }) return null

        val normalized = when {
            compact.count { it == ',' } > 1 -> return null
            ',' in compact -> compact.replace(".", "").replace(',', '.')
            compact.count { it == '.' } > 1 -> compact.replace(".", "")
            '.' in compact -> {
                val before = compact.substringBefore('.')
                val after = compact.substringAfter('.')
                if (after.length == 3 && before.length <= 3) {
                    before + after
                } else {
                    compact
                }
            }
            else -> compact
        }

        return normalized.toBigDecimalOrNull()?.takeIf { it.scale() <= 2 }
    }

    fun formatRupiahInput(value: String): String {
        val compact = value
            .trim()
            .replace(Regex("(?i)rp"), "")
            .replace(Regex("\\s+"), "")

        if (compact.isBlank()) return ""

        val decimalSeparatorIndex = compact.indexOf(',')
        val integerPart = if (decimalSeparatorIndex >= 0) {
            compact.substring(0, decimalSeparatorIndex)
        } else {
            compact
        }
        val integerDigits = integerPart.filter { it.isDigit() }
        if (integerDigits.isBlank()) return ""

        val groupedInteger = integerDigits
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        if (decimalSeparatorIndex < 0) return groupedInteger

        val decimalDigits = compact
            .substring(decimalSeparatorIndex + 1)
            .filter { it.isDigit() }
            .take(2)

        return "$groupedInteger,$decimalDigits"
    }
}
