package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class ResendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(preAuthToken: String) =
        authRepository.resendOtp(preAuthToken)
}
