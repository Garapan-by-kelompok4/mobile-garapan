package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(page: Int = 1, limit: Int = 20): Resource<List<Notification>>
    suspend fun getUnreadCount(): Resource<Int>
    suspend fun markRead(id: String): Resource<Notification>
    suspend fun markAllRead(): Resource<Int>
}
