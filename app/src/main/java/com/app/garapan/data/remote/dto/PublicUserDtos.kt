package com.app.garapan.data.remote.dto

data class PublicUserDto(
    val id: String,
    val role: String,
    val createdAt: String,
    val mahasiswa: PublicMahasiswaDto?,
    val klien: PublicKlienDto?
)

data class PublicMahasiswaDto(
    val id: String,
    val fullName: String?,
    val avatarUrl: String?,
    val university: String,
    val skills: List<String>,
    val bio: String,
    val rating: Double
)

data class PublicKlienDto(
    val id: String,
    val companyName: String?,
    val bio: String
)
