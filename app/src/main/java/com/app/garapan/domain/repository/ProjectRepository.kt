package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectListFilters

interface ProjectRepository {
    suspend fun getProjectList(filters: ProjectListFilters = ProjectListFilters()): Resource<List<Project>>
    suspend fun getProjectDetail(id: String): Resource<Project>
}
