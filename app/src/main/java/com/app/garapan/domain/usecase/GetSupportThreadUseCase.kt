package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.SupportChatRepository
import javax.inject.Inject

class GetSupportThreadUseCase @Inject constructor(
    private val supportChatRepository: SupportChatRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = DEFAULT_LIMIT) =
        supportChatRepository.getMySupportThread(page, limit)

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
