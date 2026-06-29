package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.MarkAllNotificationsReadResponseDto
import com.app.garapan.data.remote.dto.NotificationDto
import com.app.garapan.data.remote.dto.NotificationListResponseDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("notification")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): NotificationListResponseDto

    @PATCH("notification/{id}/read")
    suspend fun markRead(@Path("id") id: String): NotificationDto

    @PATCH("notification/read-all")
    suspend fun markAllRead(): MarkAllNotificationsReadResponseDto
}
