package com.app.garapan.presentation.util

import java.util.Locale

object RatingFormatter {
    fun format(rating: Float): String =
        String.format(Locale.US, "%.1f", rating)
}
