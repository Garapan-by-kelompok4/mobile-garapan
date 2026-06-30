package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.OrderChatRepository
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val orderChatRepository: OrderChatRepository
) {
    suspend operator fun invoke() = orderChatRepository.getConversations()
}
