package com.app.garapan.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileFormOptionsTest {

    @Test
    fun `build and parse mahasiswa bio round trip`() {
        val bio = buildMahasiswaBio("Information Systems", "1-3 years")
        assertEquals("Information Systems | 1-3 years", bio)
        assertEquals(
            MahasiswaBioParts("Information Systems", "1-3 years"),
            parseMahasiswaBio(bio)
        )
    }

    @Test
    fun `build and parse client bio round trip`() {
        val bio = buildClientBio("Technology", setOf("Mobile App", "Web Development"))
        assertEquals("Technology | Mobile App, Web Development", bio)
        assertEquals(
            "Technology" to setOf("Mobile App", "Web Development"),
            parseClientBio(bio)
        )
    }

    @Test
    fun `setup and edit profile share status options`() {
        assertEquals(ProfileStatus.entries, ProfileFormOptions.statusOptions)
    }
}
