package com.app.garapan.data.remote.dto

data class UserDto(
    val id: String,
    val email: String,
    val role: String,
    val emailVerified: Boolean,
    val deviceToken: String?,
    val twoFactorEnabled: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val mahasiswa: MahasiswaProfileDto?,
    val klien: KlienProfileDto?
)

data class MahasiswaProfileDto(
    val id: String,
    val userId: String,
    val university: String,
    val skills: List<String>,
    val bio: String,
    val walletBalance: String,
    val rating: Double,
    val suggestedKategoriId: String? = null,
    val suggestedKategori: KategoriDto? = null
)

data class KlienProfileDto(
    val id: String,
    val userId: String,
    val companyName: String?,
    val bio: String,
    val walletBalance: String
)

data class UpdateProfileRequestDto(
    val university: String? = null,
    val skills: List<String>? = null,
    val bio: String? = null,
    val companyName: String? = null,
    val deviceToken: String? = null
)
