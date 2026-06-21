package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(preAuthToken: String, otp: String) =
        authRepository.verifyTwoFactor(preAuthToken, otp)
}
