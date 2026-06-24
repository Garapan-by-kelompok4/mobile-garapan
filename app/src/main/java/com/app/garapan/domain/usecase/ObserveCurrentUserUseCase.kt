package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.SessionRepository
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke() = sessionRepository.currentUser

    fun snapshot(): User? = sessionRepository.peekCurrentUser()
}
