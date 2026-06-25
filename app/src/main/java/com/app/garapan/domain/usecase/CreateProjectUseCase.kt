package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(params: CreateProjectParams) =
        projectRepository.createProject(params)
}
