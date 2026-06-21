package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String, newPassword: String) =
        authRepository.resetPassword(token, newPassword)
}
