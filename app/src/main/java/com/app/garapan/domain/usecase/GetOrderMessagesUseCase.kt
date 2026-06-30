package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.OrderChatRepository
import javax.inject.Inject

class GetOrderMessagesUseCase @Inject constructor(
    private val orderChatRepository: OrderChatRepository
) {
    suspend operator fun invoke(conversationId: String, page: Int = 1, limit: Int = DEFAULT_LIMIT) =
        orderChatRepository.getMessages(conversationId, page, limit)

    companion object {
        const val DEFAULT_LIMIT = 30
    }
}
