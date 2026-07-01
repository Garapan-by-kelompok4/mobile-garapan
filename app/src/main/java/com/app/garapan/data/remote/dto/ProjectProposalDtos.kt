package com.app.garapan.data.remote.dto

data class ProjectProposalDto(
    val id: String,
    val projectId: String,
    val mahasiswaId: String,
    val message: String,
    val proposedPrice: String,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val mahasiswa: ProjectMahasiswaDto? = null,
    val project: ProjectProposalProjectDto? = null
)

data class ProjectProposalProjectDto(
    val id: String,
    val title: String,
    val budget: String,
    val deadline: String,
    val status: String,
    val imageUrl: String? = null
)

data class ProjectProposalListResponseDto(
    val data: List<ProjectProposalDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class SubmitProposalRequestDto(
    val message: String,
    val proposedPrice: String
)
