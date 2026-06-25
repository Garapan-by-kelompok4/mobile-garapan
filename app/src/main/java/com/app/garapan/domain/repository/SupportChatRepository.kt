package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.SupportMessage

interface SupportChatRepository {
    suspend fun getMySupportThread(): Resource<List<SupportMessage>>
    suspend fun sendMessage(message: String): Resource<SupportMessage>
}
