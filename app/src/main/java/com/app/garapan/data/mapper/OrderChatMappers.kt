package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ActiveOrderDto
import com.app.garapan.data.remote.dto.ConversationCounterpartyDto
import com.app.garapan.data.remote.dto.ConversationDto
import com.app.garapan.data.remote.dto.OpenConversationResponseDto
import com.app.garapan.data.remote.dto.OrderChatMessageDto
import com.app.garapan.data.remote.dto.OrderChatPageDto
import com.app.garapan.domain.model.ActiveOrder
import com.app.garapan.domain.model.Conversation
import com.app.garapan.domain.model.OpenConversationResult
import com.app.garapan.domain.model.OrderChatMessage
import com.app.garapan.domain.model.OrderChatPage
import com.app.garapan.domain.model.PesananStatus
import java.util.UUID

private const val FILE_MESSAGE_TYPE = "FILE"

fun ConversationDto.toDomain(): Conversation {
    val cp = counterparty
    return Conversation(
        conversationId = conversationId,
        counterpartyId = cp?.id,
        counterpartyName = cp.resolveDisplayName(),
        lastMessage = lastMessage?.message,
        lastMessageIsFile = lastMessage?.messageType.equals(FILE_MESSAGE_TYPE, ignoreCase = true),
        lastMessageAt = lastMessage?.createdAt,
        unreadCount = unreadCount ?: 0,
        activeOrder = activeOrder?.toDomain()
    )
}

fun OpenConversationResponseDto.toDomain(): OpenConversationResult = OpenConversationResult(
    conversationId = conversationId,
    counterpartyName = counterparty.resolveDisplayName(),
    activeOrder = activeOrder?.toDomain()
)

private fun ActiveOrderDto.toDomain(): ActiveOrder = ActiveOrder(
    pesananId = pesananId,
    status = PesananStatus.fromApiValue(status),
    title = title
)

private fun ConversationCounterpartyDto?.resolveDisplayName(): String {
    if (this == null) return "Pengguna"
    return displayName?.takeIf { it.isNotBlank() }
        ?: companyName?.takeIf { it.isNotBlank() }
        ?: university?.takeIf { it.isNotBlank() }
        ?: email?.substringBefore('@')?.takeIf { it.isNotBlank() }
        ?: "Pengguna"
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
