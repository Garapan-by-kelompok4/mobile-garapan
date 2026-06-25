package com.app.garapan.presentation.util

import java.util.Locale

object CurrencyFormatter {
    fun formatRupiah(amount: Double): String {
        val formatted = String.format(Locale("id", "ID"), "%,.0f", amount).replace(',', '.')
        return "Rp $formatted"
    }
}
