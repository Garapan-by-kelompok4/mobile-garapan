package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.ConversationDto
import com.app.garapan.data.remote.dto.OpenConversationRequestDto
import com.app.garapan.data.remote.dto.OpenConversationResponseDto
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

/** Person-pair chat — REST. Live updates come from polling the message history. */
interface ChatApi {
    @GET("chat")
    suspend fun getConversations(): List<ConversationDto>

    @POST("chat/conversations")
    suspend fun openConversation(
        @Body request: OpenConversationRequestDto
    ): OpenConversationResponseDto

    @GET("chat/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): OrderChatPageDto

    @POST("chat/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: SendOrderMessageRequestDto
    ): OrderChatMessageDto

    @PATCH("chat/{conversationId}/read")
    suspend fun markRead(@Path("conversationId") conversationId: String): JsonElement
}
