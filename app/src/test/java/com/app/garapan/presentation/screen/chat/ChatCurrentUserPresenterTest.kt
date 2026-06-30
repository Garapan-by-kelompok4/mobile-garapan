package com.app.garapan.presentation.screen.chat

import com.app.garapan.domain.model.KlienProfile
import com.app.garapan.domain.model.ProfileStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.SocialAccounts
import com.app.garapan.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatCurrentUserPresenterTest {

    @Test
    fun `uses current user display name and avatar for sent message profile`() {
        val user = User(
            id = "user-1",
            email = "andi@example.com",
            role = Role.KLIEN,
            emailVerified = true,
            deviceToken = null,
            twoFactorEnabled = false,
            createdAt = "",
            updatedAt = "",
            mahasiswa = null,
            klien = KlienProfile(
                id = "client-1",
                userId = "user-1",
                companyName = "Garapan Demo Client",
                bio = "",
                walletBalance = "0",
                avatarUrl = "https://example.com/avatar.png"
            ),
            displayName = "Andi Pratama",
            phoneNumber = null,
            status = ProfileStatus.INDIVIDUAL,
            socialAccounts = SocialAccounts()
        )

        val profile = ChatCurrentUserPresenter.from(user)

        assertEquals("AP", profile.initials)
        assertEquals("https://example.com/avatar.png", profile.avatarUrl)
    }
}
