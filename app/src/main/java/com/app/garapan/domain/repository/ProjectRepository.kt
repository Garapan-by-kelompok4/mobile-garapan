package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters
import com.app.garapan.domain.model.UpdateProjectParams

interface ProjectRepository {
    suspend fun getProjectList(filters: ProjectListFilters = ProjectListFilters()): Resource<List<Project>>
    suspend fun getMyProjectList(filters: ProjectListFilters = ProjectListFilters()): Resource<List<Project>>
    suspend fun getProjectDetail(id: String): Resource<Project>
    suspend fun createProject(params: CreateProjectParams): Resource<Project>
    suspend fun takeProject(id: String): Resource<Pesanan>
    suspend fun updateProject(id: String, params: UpdateProjectParams): Resource<Project>
    suspend fun deleteProject(id: String): Resource<Unit>
}
