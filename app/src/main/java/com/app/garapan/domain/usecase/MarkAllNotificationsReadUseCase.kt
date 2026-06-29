package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke() = notificationRepository.markAllRead()
}
