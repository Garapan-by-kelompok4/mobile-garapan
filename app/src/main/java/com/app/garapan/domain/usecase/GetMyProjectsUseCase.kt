package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.repository.ProjectRepository
import javax.inject.Inject

class GetMyProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(filters: ProjectListFilters = ProjectListFilters()) =
        projectRepository.getMyProjectList(filters)
}
