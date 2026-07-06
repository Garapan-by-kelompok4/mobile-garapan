package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.SupportMessageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportChatMappersTest {

    @Test
    fun `maps FILE message with attachment fields`() {
        val dto = SupportMessageDto(
            id = "msg-1",
            userId = "user-1",
            adminId = "admin-1",
            senderId = "user-1",
            senderRole = "MAHASISWA",
            message = "",
            messageType = "FILE",
            fileUrl = "https://res.cloudinary.com/example/doc.pdf",
            fileName = "doc.pdf",
            fileSize = 12345,
            mimeType = "application/pdf",
            createdAt = "2026-07-06T10:00:00.000Z"
        )

        val domain = dto.toDomain()

        assertEquals("msg-1", domain.id)
        assertTrue(domain.isFile)
        assertEquals("doc.pdf", domain.fileName)
        assertEquals("https://res.cloudinary.com/example/doc.pdf", domain.fileUrl)
        assertTrue(domain.isFromUser)
    }

    @Test
    fun `maps TEXT message without attachment fields`() {
        val dto = SupportMessageDto(
            id = "msg-2",
            userId = "user-1",
            adminId = "admin-1",
            senderId = "admin-1",
            senderRole = "ADMIN",
            message = "Halo, ada yang bisa dibantu?",
            messageType = "TEXT",
            createdAt = "2026-07-06T10:01:00.000Z"
        )

        val domain = dto.toDomain()

        assertFalse(domain.isFile)
        assertEquals("Halo, ada yang bisa dibantu?", domain.message)
        assertFalse(domain.isFromUser)
    }
}
