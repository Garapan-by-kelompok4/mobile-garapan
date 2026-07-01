package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.ChatAttachmentUpload
import com.app.garapan.domain.repository.OrderChatRepository
import javax.inject.Inject

class SendOrderAttachmentUseCase @Inject constructor(
    private val orderChatRepository: OrderChatRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        attachment: ChatAttachmentUpload,
        pesananId: String? = null
    ) = orderChatRepository.sendAttachment(conversationId, attachment, pesananId)
}
