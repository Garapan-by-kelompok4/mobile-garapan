package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class DeleteProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(id: String) = projectRepository.deleteProject(id)
}
