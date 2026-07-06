package com.app.garapan.presentation.screen.chat

import com.app.garapan.domain.model.ActiveOrder
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.PesananStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatPeerProfilePresenterTest {

    @Test
    fun needsHydration_whenPeerNameBlank() {
        assertTrue(ChatPeerProfilePresenter.needsHydration(""))
        assertTrue(ChatPeerProfilePresenter.needsHydration("   "))
    }

    @Test
    fun needsHydration_whenPeerNamePresent() {
        assertFalse(ChatPeerProfilePresenter.needsHydration("Budi Santoso"))
    }

    @Test
    fun fromConversation_mapsNameInitialsAvatarAndActiveOrder() {
        val conversation = Conversation(
            conversationId = "conv-1",
            counterpartyId = "user-1",
            counterpartyName = "Budi Santoso",
            counterpartyAvatarUrl = "https://example.com/avatar.jpg",
            lastMessage = "halo",
            lastMessageIsFile = false,
            lastMessageAt = "2026-07-06T15:29:00.000Z",
            unreadCount = 1,
            activeOrder = ActiveOrder(
                pesananId = "pesanan-1",
                status = PesananStatus.IN_PROGRESS,
                title = "Desain Logo"
            )
        )

        val profile = ChatPeerProfilePresenter.from(conversation)

        assertEquals("Budi Santoso", profile.name)
        assertEquals("BS", profile.initials)
        assertEquals("https://example.com/avatar.jpg", profile.avatarUrl)
        assertEquals("pesanan-1", profile.activeOrder?.pesananId)
    }

    @Test
    fun fromConversation_blankAvatarUrlBecomesNull() {
        val conversation = Conversation(
            conversationId = "conv-2",
            counterpartyId = "user-2",
            counterpartyName = "Andi",
            counterpartyAvatarUrl = "   ",
            lastMessage = null,
            lastMessageIsFile = false,
            lastMessageAt = null,
            unreadCount = 0,
            activeOrder = null
        )

        val profile = ChatPeerProfilePresenter.from(conversation)

        assertNull(profile.avatarUrl)
        assertEquals("AN", profile.initials)
    }

    @Test
    fun initialsOf_emptyName_returnsQuestionMark() {
        assertEquals("?", ChatPeerProfilePresenter.initialsOf(""))
    }
}
