package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.SupportChatRepository
import javax.inject.Inject

class SendSupportMessageUseCase @Inject constructor(
    private val supportChatRepository: SupportChatRepository
) {
    suspend operator fun invoke(message: String) = supportChatRepository.sendMessage(message)
}
