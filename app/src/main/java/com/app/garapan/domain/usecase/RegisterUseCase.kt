package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.Role
import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, role: Role) =
        authRepository.register(email, password, role)
}
