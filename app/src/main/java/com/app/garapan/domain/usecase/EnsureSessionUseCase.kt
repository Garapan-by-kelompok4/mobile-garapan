package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.repository.SessionRepository
import javax.inject.Inject

sealed interface EnsureSessionResult {
    data object Ready : EnsureSessionResult
    data object RequiresLogin : EnsureSessionResult
}

class EnsureSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val checkAuthTokenUseCase: CheckAuthTokenUseCase,
    private val loadSessionUseCase: LoadSessionUseCase
) {
    suspend operator fun invoke(): EnsureSessionResult {
        if (sessionRepository.peekCurrentUser() != null) {
            return EnsureSessionResult.Ready
        }
        if (!checkAuthTokenUseCase()) {
            return EnsureSessionResult.RequiresLogin
        }
        return when (loadSessionUseCase()) {
            is Resource.Success -> EnsureSessionResult.Ready
            is Resource.Error -> EnsureSessionResult.RequiresLogin
            Resource.Loading -> EnsureSessionResult.RequiresLogin
        }
    }
}
