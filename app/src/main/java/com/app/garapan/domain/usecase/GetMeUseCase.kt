package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class GetMeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.getMe()
}
