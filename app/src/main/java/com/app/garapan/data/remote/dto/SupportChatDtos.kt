package com.app.garapan.data.remote.dto

data class SupportMessageDto(
    val id: String? = null,
    val userId: String? = null,
    val adminId: String? = null,
    val senderId: String? = null,
    val senderRole: String? = null,
    val message: String? = null,
    val content: String? = null,
    val messageType: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val readAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class SendSupportMessageRequestDto(
    val message: String
)
