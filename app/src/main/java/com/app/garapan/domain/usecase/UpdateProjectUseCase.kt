package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.UpdateProjectParams
import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class UpdateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(id: String, params: UpdateProjectParams) =
        projectRepository.updateProject(id, params)
}
