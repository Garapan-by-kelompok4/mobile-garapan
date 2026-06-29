package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 50) =
        notificationRepository.getNotifications(page = page, limit = limit)
}
