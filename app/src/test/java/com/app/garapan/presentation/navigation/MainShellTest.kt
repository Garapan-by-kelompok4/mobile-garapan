package com.app.garapan.presentation.navigation

import com.app.garapan.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Test

class MainShellTest {

    @Test
    fun clientTabsIncludeCreateActionAndChat() {
        val labels = mainTabLabelsForRole(Role.KLIEN)

        assertEquals(
            listOf("Home", "Pesanan", "New", "Chat", "Profile"),
            labels
        )
    }

    @Test
    fun mahasiswaTabsStayUnchanged() {
        val labels = mainTabLabelsForRole(Role.MAHASISWA)

        assertEquals(
            listOf("Home", "My Jasa", "Pesanan", "Chat", "Profile"),
            labels
        )
    }
}
