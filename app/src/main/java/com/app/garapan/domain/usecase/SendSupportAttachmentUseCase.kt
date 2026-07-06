package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.ChatAttachmentUpload
import com.app.garapan.domain.repository.SupportChatRepository
import javax.inject.Inject

class SendSupportAttachmentUseCase @Inject constructor(
    private val supportChatRepository: SupportChatRepository
) {
    suspend operator fun invoke(attachment: ChatAttachmentUpload) =
        supportChatRepository.sendAttachment(attachment)
}
