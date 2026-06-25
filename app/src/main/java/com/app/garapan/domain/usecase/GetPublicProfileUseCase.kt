package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.UsersRepository
import javax.inject.Inject

class GetPublicProfileUseCase @Inject constructor(
    private val usersRepository: UsersRepository
) {
    suspend operator fun invoke(userId: String) = usersRepository.getPublicProfile(userId)
}
