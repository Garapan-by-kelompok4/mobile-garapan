package com.app.garapan.presentation.screen.jasa_detail

import org.junit.Assert.assertEquals
import org.junit.Test

class JasaDetailScreenTest {

    @Test
    fun `rating summary shows rows for one through five stars`() {
        assertEquals(listOf(5, 4, 3, 2, 1), RatingSummaryStars)
    }
}
