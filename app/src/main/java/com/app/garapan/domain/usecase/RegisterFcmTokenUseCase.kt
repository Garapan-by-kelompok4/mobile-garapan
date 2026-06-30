package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository
import com.app.garapan.domain.repository.SessionRepository
import javax.inject.Inject

class RegisterFcmTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(token: String): Resource<User> {
        if (token.isBlank()) return Resource.Error("FCM token is blank.")
        return when (val result = authRepository.updateProfile(UpdateProfileParams(deviceToken = token))) {
            is Resource.Success -> {
                sessionRepository.setUser(result.data)
                result
            }
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
    }
}
