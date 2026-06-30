package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.OrderChatRepository
import javax.inject.Inject

class OpenConversationUseCase @Inject constructor(
    private val orderChatRepository: OrderChatRepository
) {
    suspend operator fun invoke(counterpartyId: String) =
        orderChatRepository.openConversation(counterpartyId)
}
