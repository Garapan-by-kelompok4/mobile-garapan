package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.LaporanDto
import com.app.garapan.data.remote.dto.PesananDto
import com.app.garapan.domain.model.LaporanStatus
import com.app.garapan.domain.model.PesananStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PesananLaporanMapperTest {

    @Test
    fun mapsLatestLaporanFromPesananResponse() {
        val pesanan = PesananDto(
            id = "pesanan-1",
            klienId = "klien-1",
            mahasiswaId = "mahasiswa-1",
            totalPrice = 100_000.0,
            status = "DISPUTED",
            createdAt = "2026-01-01T00:00:00.000Z",
            updatedAt = "2026-01-01T00:00:00.000Z",
            laporan = listOf(
                LaporanDto(
                    id = "laporan-1",
                    reporterId = "user-1",
                    reason = "Hasil tidak sesuai brief.",
                    status = "PENDING",
                    createdAt = "2026-01-02T00:00:00.000Z"
                )
            )
        )

        val domain = pesanan.toDomain()

        assertEquals(PesananStatus.DISPUTED, domain.status)
        assertNotNull(domain.laporan)
        assertEquals("laporan-1", domain.laporan?.id)
        assertEquals(LaporanStatus.PENDING, domain.laporan?.status)
        assertEquals("Hasil tidak sesuai brief.", domain.laporan?.reason)
    }
}
