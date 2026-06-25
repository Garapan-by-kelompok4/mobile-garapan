package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.SupportMessageDto
import com.app.garapan.domain.model.SupportMessage
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.util.UUID

fun SupportMessageDto.toDomain(): SupportMessage {
    val role = senderRole?.uppercase()
    val isFromUser = when {
        role in setOf("ADMIN", "SUPPORT") -> false
        role != null -> true
        !senderId.isNullOrBlank() && !userId.isNullOrBlank() -> senderId == userId
        !senderId.isNullOrBlank() && !adminId.isNullOrBlank() -> senderId != adminId
        adminId.isNullOrBlank() -> true
        else -> false
    }

    return SupportMessage(
        id = id ?: UUID.randomUUID().toString(),
        userId = userId,
        adminId = adminId,
        senderId = senderId,
        senderRole = senderRole,
        message = message ?: content.orEmpty(),
        createdAt = createdAt ?: updatedAt,
        isFromUser = isFromUser
    )
}

fun JsonElement.toSupportMessage(gson: Gson): SupportMessage =
    findMessageElement().let { element ->
        gson.fromJson(element, SupportMessageDto::class.java).toDomain()
    }

fun JsonElement.toSupportMessage(gson: Gson, fallbackMessage: String): SupportMessage {
    val element = findMessageElement()
    if (!element.isJsonObject) {
        return SupportMessageDto(message = fallbackMessage).toDomain()
    }
    return gson.fromJson(element, SupportMessageDto::class.java).toDomain()
}

fun JsonElement.toSupportMessages(gson: Gson): List<SupportMessage> {
    val messageArray = findMessageArrayElement()
    val listType = object : TypeToken<List<SupportMessageDto>>() {}.type
    return gson.fromJson<List<SupportMessageDto>>(messageArray, listType).map { it.toDomain() }
}

private fun JsonElement.findMessageElement(): JsonElement {
    if (isJsonArray) return asJsonArray.lastOrNull() ?: JsonObject()
    val root = asJsonObjectOrNull() ?: return this
    val candidate = root.firstPresent("data", "chat", "liveChat") ?: return this
    if (candidate.isJsonArray) return candidate.asJsonArray.lastOrNull() ?: JsonObject()
    return candidate
}

private fun JsonElement.findMessageArrayElement(): JsonArray {
    if (isJsonArray) return asJsonArray
    val root = asJsonObjectOrNull() ?: return JsonArray()
    root.findMessageArrayCandidate()?.let { return it }
    return JsonArray().apply {
        if (root.looksLikeMessage()) add(root)
    }
}

private fun JsonElement.asJsonObjectOrNull(): JsonObject? =
    takeIf { it.isJsonObject }?.asJsonObject

private fun JsonObject.firstPresent(vararg keys: String): JsonElement? =
    keys.firstNotNullOfOrNull { key ->
        get(key)?.takeUnless { it.isJsonNull }
    }

private fun JsonObject.findMessageArrayCandidate(): JsonArray? {
    firstPresent("data", "messages", "thread", "items")?.let { candidate ->
        if (candidate.isJsonArray) return candidate.asJsonArray
        val nested = candidate.asJsonObjectOrNull()
        nested?.findMessageArrayCandidate()?.let { return it }
    }
    return null
}

private fun JsonObject.looksLikeMessage(): Boolean =
    has("message") || has("content") || has("senderId") || has("senderRole")
