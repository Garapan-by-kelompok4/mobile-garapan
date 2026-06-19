package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(params: UpdateProfileParams) =
        authRepository.updateProfile(params)
}
