package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Notification
import javax.inject.Inject

class MarkMatchingNotificationsReadUseCase @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase
) {
    suspend operator fun invoke(matcher: (Notification) -> Boolean): Int {
        val notifications = when (val result = getNotificationsUseCase(limit = 50)) {
            is Resource.Success -> result.data
            else -> return 0
        }
        var marked = 0
        for (notification in notifications) {
            if (!notification.read && matcher(notification)) {
                when (markNotificationReadUseCase(notification.id)) {
                    is Resource.Success -> marked++
                    else -> Unit
                }
            }
        }
        return marked
    }
}
