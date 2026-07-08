package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.TakenProject
import com.app.garapan.domain.model.UpdateProjectParams

interface ProjectRepository {
    suspend fun getProjectList(filters: ProjectListFilters = ProjectListFilters()): Resource<List<Project>>
    suspend fun getMyProjectList(filters: ProjectListFilters = ProjectListFilters()): Resource<List<Project>>
    suspend fun getProjectDetail(id: String): Resource<Project>
    suspend fun createProject(params: CreateProjectParams): Resource<Project>
    suspend fun updateProject(id: String, params: UpdateProjectParams): Resource<Project>
    suspend fun deleteProject(id: String): Resource<Unit>
    suspend fun submitProposal(projectId: String, message: String, proposedPrice: Double): Resource<ProjectProposal>
    suspend fun withdrawProposal(projectId: String): Resource<Unit>
    suspend fun getProjectProposals(projectId: String): Resource<List<ProjectProposal>>
    suspend fun getMyProposals(page: Int = 1, limit: Int = 20): Resource<List<ProjectProposal>>
    suspend fun getMyTakenProjects(page: Int = 1, limit: Int = 20): Resource<List<TakenProject>>
    suspend fun acceptProposal(projectId: String, proposalId: String): Resource<Pesanan>
    suspend fun rejectProposal(projectId: String, proposalId: String): Resource<Unit>
}
