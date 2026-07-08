package com.app.garapan.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PesananDisplayMapperTest {

    @Test
    fun `order title uses project title for project based pesanan`() {
        val title = PesananDisplayMapper.orderTitle(
            jasaTitle = "",
            projectId = "project-1",
            projectTitle = "landing page"
        )

        assertEquals("Pembayaran Proyek: landing page", title)
    }

    @Test
    fun `order title falls back to jasa title`() {
        val title = PesananDisplayMapper.orderTitle(
            jasaTitle = "layanan mahasiswa mantap",
            projectId = null,
            projectTitle = ""
        )

        assertEquals("Pembayaran Jasa: layanan mahasiswa mantap", title)
    }
}
