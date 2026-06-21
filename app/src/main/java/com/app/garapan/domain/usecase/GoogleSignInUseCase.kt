package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.Role
import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String, role: Role? = null) =
        authRepository.googleSignIn(idToken, role)
}
