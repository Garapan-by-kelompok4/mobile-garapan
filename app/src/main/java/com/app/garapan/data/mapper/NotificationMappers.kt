package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.NotificationDto
import com.app.garapan.data.remote.dto.NotificationMetaDto
import com.app.garapan.domain.model.Notification
import com.app.garapan.domain.model.NotificationMeta
import com.app.garapan.domain.model.NotificationType

fun NotificationDto.toDomain() = Notification(
    id = id,
    title = title,
    body = body,
    type = NotificationType.fromApiValue(type),
    read = read,
    createdAt = createdAt,
    meta = meta?.toDomain()
)

private fun NotificationMetaDto.toDomain() = NotificationMeta(
    pesananId = pesananId,
    reviewId = reviewId,
    jasaId = jasaId
)
