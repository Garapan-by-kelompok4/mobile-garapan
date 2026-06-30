package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.OrderChatRepository
import javax.inject.Inject

class SendOrderMessageUseCase @Inject constructor(
    private val orderChatRepository: OrderChatRepository
) {
    suspend operator fun invoke(pesananId: String, message: String) =
        orderChatRepository.sendMessage(pesananId, message)
}
