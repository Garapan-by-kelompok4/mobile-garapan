package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.remote.api.NotificationApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Notification
import com.app.garapan.domain.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override suspend fun getNotifications(page: Int, limit: Int): Resource<List<Notification>> =
        safeApiCall {
            notificationApi.getNotifications(page = page, limit = limit).data.map { it.toDomain() }
        }

    override suspend fun markRead(id: String): Resource<Notification> =
        safeApiCall {
            notificationApi.markRead(id).toDomain()
        }

    override suspend fun markAllRead(): Resource<Int> =
        safeApiCall {
            notificationApi.markAllRead().updated
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
