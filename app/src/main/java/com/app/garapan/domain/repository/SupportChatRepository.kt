package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.SupportMessage
import com.app.garapan.domain.model.SupportThreadPage

interface SupportChatRepository {
    suspend fun getMySupportThread(page: Int, limit: Int): Resource<SupportThreadPage>
    suspend fun sendMessage(message: String): Resource<SupportMessage>
    suspend fun markMyThreadRead(): Resource<Unit>
}
