package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.PesananDto
import com.app.garapan.data.remote.dto.PesananKlienDto
import com.app.garapan.data.remote.dto.PesananMahasiswaDto
import com.app.garapan.data.remote.dto.PesananUserDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PesananMappersTest {

    private fun baseDto(
        klien: PesananKlienDto? = null,
        mahasiswa: PesananMahasiswaDto? = null
    ) = PesananDto(
        id = "pesanan-1",
        klienId = "klien-1",
        mahasiswaId = "mhs-1",
        totalPrice = 25_000_000.0,
        status = "PENDING",
        createdAt = "2026-07-01T00:00:00.000Z",
        updatedAt = "2026-07-01T00:00:00.000Z",
        klien = klien,
        mahasiswa = mahasiswa
    )

    @Test
    fun prefersMahasiswaFullNameOverUserEmail() {
        val dto = baseDto(
            mahasiswa = PesananMahasiswaDto(
                id = "mhs-1",
                fullName = "Andi Pratama",
                university = "Universitas Indonesia",
                user = PesananUserDto(id = "user-1", email = "andi.mahasiswa@garapan.test")
            )
        )

        val pesanan = dto.toDomain()

        assertEquals("Andi Pratama", pesanan.workerName)
    }

    @Test
    fun fallsBackToEmailWhenFullNameAndDisplayNameAreMissing() {
        val dto = baseDto(
            mahasiswa = PesananMahasiswaDto(
                id = "mhs-1",
                fullName = null,
                university = "Universitas Indonesia",
                user = PesananUserDto(id = "user-1", email = "andi.mahasiswa@garapan.test")
            )
        )

        val pesanan = dto.toDomain()

        assertEquals("andi.mahasiswa@garapan.test", pesanan.workerName)
    }

    @Test
    fun prefersKlienCompanyNameOverUserEmail() {
        val dto = baseDto(
            klien = PesananKlienDto(
                id = "klien-1",
                companyName = "Garapan Demo Client",
                user = PesananUserDto(id = "user-2", email = "budi.klien@garapan.test")
            )
        )

        val pesanan = dto.toDomain()

        assertEquals("Garapan Demo Client", pesanan.clientLabel)
    }
}
