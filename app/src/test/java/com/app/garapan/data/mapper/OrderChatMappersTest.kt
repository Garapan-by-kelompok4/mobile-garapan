package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ActiveOrderDto
import com.app.garapan.data.remote.dto.ConversationCounterpartyDto
import com.app.garapan.data.remote.dto.ConversationDto
import com.app.garapan.data.remote.dto.OrderChatMessageDto
import com.app.garapan.domain.model.PesananStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class OrderChatMappersTest {

    @Test
    fun conversationWithoutLastMessageMapsAsEmptyThread() {
        val dto = ConversationDto(
            conversationId = "conv-1",
            counterparty = ConversationCounterpartyDto(
                id = "worker-user",
                displayName = "Andi"
            ),
            lastMessage = null,
            unreadCount = 0,
            activeOrder = null
        )

        val conversation = dto.toDomain()

        assertEquals("conv-1", conversation.conversationId)
        assertEquals("worker-user", conversation.counterpartyId)
        assertEquals("Andi", conversation.counterpartyName)
        assertNull(conversation.lastMessage)
        assertFalse(conversation.lastMessageIsFile)
        assertNull(conversation.lastMessageAt)
        assertEquals(0, conversation.unreadCount)
        assertNull(conversation.activeOrder)
    }

    @Test
    fun conversationWithActiveOrderMapsStatusAndTitle() {
        val dto = ConversationDto(
            conversationId = "conv-2",
            counterparty = ConversationCounterpartyDto(
                id = "worker-2",
                email = "budi@example.com"
            ),
            lastMessage = OrderChatMessageDto(
                message = "Halo",
                messageType = "TEXT",
                createdAt = "2026-06-30T12:00:00.000Z"
            ),
            unreadCount = 2,
            activeOrder = ActiveOrderDto(
                pesananId = "pesanan-2",
                status = "IN_PROGRESS",
                title = "Landing Page"
            )
        )

        val conversation = dto.toDomain()

        assertEquals("budi", conversation.counterpartyName)
        assertEquals(PesananStatus.IN_PROGRESS, conversation.activeOrder?.status)
        assertEquals("pesanan-2", conversation.activeOrder?.pesananId)
        assertEquals("Landing Page", conversation.activeOrder?.title)
        assertEquals(2, conversation.unreadCount)
    }

    @Test
    fun fileLastMessageMapsAsFileConversation() {
        val dto = ConversationDto(
            conversationId = "conv-3",
            counterparty = ConversationCounterpartyDto(email = "budi@example.com"),
            lastMessage = OrderChatMessageDto(
                messageType = "FILE",
                fileName = "doc.pdf",
                createdAt = "2026-06-30T12:00:00.000Z"
            ),
            unreadCount = 1,
            activeOrder = ActiveOrderDto(
                pesananId = "pesanan-3",
                status = "DELIVERED",
                title = null
            )
        )

        val conversation = dto.toDomain()

        assertNull(conversation.lastMessage)
        assertEquals(true, conversation.lastMessageIsFile)
        assertEquals(PesananStatus.DELIVERED, conversation.activeOrder?.status)
    }

    @Test
    fun conversationWithAvatarUrlMapsToDomain() {
        val dto = ConversationDto(
            conversationId = "conv-avatar",
            counterparty = ConversationCounterpartyDto(
                id = "worker-avatar",
                displayName = "Citra",
                avatarUrl = "https://example.com/citra.jpg"
            ),
            lastMessage = null,
            unreadCount = 0,
            activeOrder = null
        )

        val conversation = dto.toDomain()

        assertEquals("https://example.com/citra.jpg", conversation.counterpartyAvatarUrl)
    }
}
