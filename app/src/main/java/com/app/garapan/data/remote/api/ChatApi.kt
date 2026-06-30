package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.ConversationDto
import com.app.garapan.data.remote.dto.OrderChatMessageDto
import com.app.garapan.data.remote.dto.OrderChatPageDto
import com.app.garapan.data.remote.dto.SendOrderMessageRequestDto
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Order (per-pesanan) chat — REST. Live updates come from polling the message history. */
interface ChatApi {
    @GET("chat")
    suspend fun getConversations(): List<ConversationDto>

    @GET("chat/{pesananId}/messages")
    suspend fun getMessages(
        @Path("pesananId") pesananId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): OrderChatPageDto

    @POST("chat/{pesananId}/messages")
    suspend fun sendMessage(
        @Path("pesananId") pesananId: String,
        @Body request: SendOrderMessageRequestDto
    ): OrderChatMessageDto

    @PATCH("chat/{pesananId}/read")
    suspend fun markRead(@Path("pesananId") pesananId: String): JsonElement
}
