package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class SubmitProjectProposalUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String, message: String, proposedPrice: Double) =
        projectRepository.submitProposal(projectId, message, proposedPrice)
}
