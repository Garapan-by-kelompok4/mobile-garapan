package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.repository.SessionRepository
import javax.inject.Inject

sealed interface EnsureSessionResult {
    data object Ready : EnsureSessionResult

    /** Session restored from the local snapshot; caller should refresh it in the background. */
    data object ReadyCached : EnsureSessionResult

    data object RequiresLogin : EnsureSessionResult

    /** Tokens look valid but the server is unreachable; caller should offer a retry. */
    data object Unavailable : EnsureSessionResult
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
        if (sessionRepository.restoreCachedUser() != null) {
            return EnsureSessionResult.ReadyCached
        }
        return when (loadSessionUseCase()) {
            is Resource.Success -> EnsureSessionResult.Ready
            // The token refresh path wipes stored tokens on a definitive auth
            // failure, so a token that survived the failed load means the error
            // was transient (offline, server down) — not a reason to log out.
            else -> if (checkAuthTokenUseCase()) {
                EnsureSessionResult.Unavailable
            } else {
                EnsureSessionResult.RequiresLogin
            }
        }
    }
}
