package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.SendSupportMessageRequestDto
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SupportChatApi {
    @GET("live-chat-admin/me")
    suspend fun getMySupportThread(): JsonElement

    @POST("live-chat-admin")
    suspend fun sendMessage(@Body request: SendSupportMessageRequestDto): JsonElement
}
