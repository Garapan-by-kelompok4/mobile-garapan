package com.app.garapan.data.remote.dto

data class JasaDto(
    val id: String,
    val mahasiswaId: String,
    val kategoriId: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val kategori: KategoriDto? = null,
    val mahasiswa: JasaMahasiswaDto? = null,
    val portofolio: List<JasaPortofolioPreviewDto>? = null,
    val rating: Double? = null,
    val reviewCount: Int? = null
)

data class JasaListResponseDto(
    val data: List<JasaDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class JasaMahasiswaDto(
    val id: String,
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val university: String? = null,
    val rating: Double? = null,
    val user: JasaMahasiswaUserDto? = null
)

data class JasaMahasiswaUserDto(
    val id: String? = null,
    val name: String? = null,
    val displayName: String? = null
)

data class JasaPortofolioPreviewDto(
    val id: String,
    val title: String,
    val imageUrl: String,
    val projectUrl: String? = null
)

data class UpdateJasaRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val kategoriId: String? = null,
    val status: String? = null
)
