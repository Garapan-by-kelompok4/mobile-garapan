package com.app.garapan.domain.model

data class SupportMessage(
    val id: String,
    val userId: String?,
    val adminId: String?,
    val senderId: String?,
    val senderRole: String?,
    val message: String,
    val isFile: Boolean,
    val fileName: String?,
    val fileUrl: String?,
    val createdAt: String?,
    val isFromUser: Boolean
)
