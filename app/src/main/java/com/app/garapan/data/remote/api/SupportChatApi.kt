package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.SendSupportMessageRequestDto
import com.app.garapan.data.remote.dto.SupportMessageDto
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface SupportChatApi {
    @GET("live-chat-admin/me")
    suspend fun getMySupportThread(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): JsonElement

    @POST("live-chat-admin")
    suspend fun sendMessage(@Body request: SendSupportMessageRequestDto): JsonElement

    @Multipart
    @POST("live-chat-admin/attachments")
    suspend fun sendSupportAttachment(
        @Part file: MultipartBody.Part
    ): SupportMessageDto

    @PATCH("live-chat-admin/me/read")
    suspend fun markMyThreadRead(): JsonElement
}
