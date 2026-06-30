package com.app.garapan.data.mapper

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
    fun conversationWithoutLastMessageMapsAsEmptyOrderChat() {
        val dto = ConversationDto(
            pesananId = "pesanan-1",
            status = "PENDING",
            counterparty = ConversationCounterpartyDto(
                id = "worker-user",
                email = "andi@example.com",
                role = "MAHASISWA",
                displayName = "Andi"
            ),
            lastMessage = null,
            unreadCount = null
        )

        val conversation = dto.toDomain()

        assertEquals("pesanan-1", conversation.pesananId)
        assertEquals(PesananStatus.PENDING, conversation.status)
        assertEquals("worker-user", conversation.counterpartyId)
        assertEquals("Andi", conversation.counterpartyName)
        assertNull(conversation.lastMessage)
        assertFalse(conversation.lastMessageIsFile)
        assertNull(conversation.lastMessageAt)
        assertEquals(0, conversation.unreadCount)
    }

    @Test
    fun fileLastMessageMapsAsFileConversation() {
        val dto = ConversationDto(
            pesananId = "pesanan-2",
            status = "DELIVERED",
            counterparty = ConversationCounterpartyDto(email = "budi@example.com"),
            lastMessage = OrderChatMessageDto(
                message = "",
                messageType = "FILE",
                createdAt = "2026-06-30T12:00:00.000Z"
            ),
            unreadCount = 2
        )

        val conversation = dto.toDomain()

        assertEquals("budi", conversation.counterpartyName)
        assertEquals(PesananStatus.DELIVERED, conversation.status)
        assertEquals("", conversation.lastMessage)
        assertEquals("2026-06-30T12:00:00.000Z", conversation.lastMessageAt)
        assertEquals(2, conversation.unreadCount)
        assertEquals(true, conversation.lastMessageIsFile)
    }
}
