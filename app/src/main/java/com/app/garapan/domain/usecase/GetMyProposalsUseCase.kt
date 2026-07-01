package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class GetMyProposalsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 20) =
        projectRepository.getMyProposals(page, limit)
}
