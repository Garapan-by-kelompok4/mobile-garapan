package com.app.garapan.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UserMessageLocalizerTest {

    @Test
    fun `maps jasa delete blocked by orders message to Indonesian`() {
        assertEquals(
            "Layanan tidak dapat dihapus karena sudah ada pesanan yang terhubung.",
            UserMessageLocalizer.localize("Jasa cannot be deleted because orders reference it")
        )
    }

    @Test
    fun `passes through unknown messages unchanged`() {
        assertEquals("Custom backend error", UserMessageLocalizer.localize("Custom backend error"))
    }
}
