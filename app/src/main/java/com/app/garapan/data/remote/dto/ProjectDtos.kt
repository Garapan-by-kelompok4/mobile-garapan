package com.app.garapan.data.remote.dto

data class ProjectDto(
    val id: String,
    val klienId: String,
    val kategoriId: String,
    val assignedMahasiswaId: String? = null,
    val title: String,
    val description: String,
    val budget: String,
    val deadline: String,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val kategori: KategoriDto? = null,
    val klien: ProjectKlienDto? = null,
    val assignedMahasiswa: ProjectMahasiswaDto? = null
)

data class ProjectListResponseDto(
    val data: List<ProjectDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class ProjectKlienDto(
    val id: String,
    val companyName: String? = null,
    val user: ProjectUserDto? = null
)

data class ProjectMahasiswaDto(
    val id: String,
    val university: String? = null,
    val rating: Double? = null,
    val user: ProjectUserDto? = null
)

data class ProjectUserDto(
    val id: String? = null,
    val name: String? = null
)
