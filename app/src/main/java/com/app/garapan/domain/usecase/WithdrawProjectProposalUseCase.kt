package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class WithdrawProjectProposalUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String) = projectRepository.withdrawProposal(projectId)
}
