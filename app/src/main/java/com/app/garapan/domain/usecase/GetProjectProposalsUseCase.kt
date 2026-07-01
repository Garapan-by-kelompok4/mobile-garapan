package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class GetProjectProposalsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = projectRepository.getProjectProposals(projectId)
}
