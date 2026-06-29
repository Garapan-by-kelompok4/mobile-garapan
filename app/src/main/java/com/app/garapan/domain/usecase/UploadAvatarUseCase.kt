package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository
import com.app.garapan.domain.repository.SessionRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(image: PortofolioImage): Resource<User> =
        when (val result = authRepository.uploadAvatar(image)) {
            is Resource.Success -> {
                sessionRepository.setUser(result.data)
                result
            }
            is Resource.Error -> result
            Resource.Loading -> Resource.Loading
        }
}
