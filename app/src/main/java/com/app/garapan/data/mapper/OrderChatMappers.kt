package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ConversationDto
import com.app.garapan.data.remote.dto.OrderChatMessageDto
import com.app.garapan.data.remote.dto.OrderChatPageDto
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.OrderChatMessage
import com.app.garapan.domain.model.OrderChatPage
import com.app.garapan.domain.model.PesananStatus
import java.util.UUID

private const val FILE_MESSAGE_TYPE = "FILE"

fun ConversationDto.toDomain(): Conversation {
    val cp = counterparty
    val name = cp?.displayName?.takeIf { it.isNotBlank() }
        ?: cp?.companyName?.takeIf { it.isNotBlank() }
        ?: cp?.university?.takeIf { it.isNotBlank() }
        ?: cp?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
        ?: "Pengguna"
    return Conversation(
        pesananId = pesananId.orEmpty(),
        status = status?.let { PesananStatus.fromApiValue(it) },
        counterpartyId = cp?.id,
        counterpartyName = name,
        lastMessage = lastMessage?.message,
        lastMessageIsFile = lastMessage?.messageType.equals(FILE_MESSAGE_TYPE, ignoreCase = true),
        lastMessageAt = lastMessage?.createdAt,
        unreadCount = unreadCount ?: 0
    )
}

fun OrderChatMessageDto.toDomain(): OrderChatMessage {
    val isFile = messageType.equals(FILE_MESSAGE_TYPE, ignoreCase = true)
    return OrderChatMessage(
        id = id ?: UUID.randomUUID().toString(),
        senderId = senderId,
        message = message.orEmpty(),
        isFile = isFile,
        fileName = fileName,
        fileUrl = fileUrl,
        createdAt = createdAt
    )
}

fun OrderChatPageDto.toDomain(requestedPage: Int, requestedLimit: Int): OrderChatPage {
    val messages = data?.map { it.toDomain() }.orEmpty()
    return OrderChatPage(
        messages = messages,
        total = total ?: messages.size,
        page = page ?: requestedPage,
        limit = limit ?: requestedLimit
    )
}
