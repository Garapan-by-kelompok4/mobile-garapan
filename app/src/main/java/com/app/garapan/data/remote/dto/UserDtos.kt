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
    val klien: KlienProfileDto?,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val status: String? = null,
    val socialAccounts: SocialAccountsDto? = null
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
    val suggestedKategori: KategoriDto? = null,
    val fullName: String? = null,
    val avatarUrl: String? = null
)

data class KlienProfileDto(
    val id: String,
    val userId: String,
    val companyName: String?,
    val bio: String,
    val walletBalance: String,
    val avatarUrl: String? = null
)

data class SocialAccountsDto(
    val linkedinUrl: String? = null
)

data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val university: String? = null,
    val skills: List<String>? = null,
    val bio: String? = null,
    val companyName: String? = null,
    val phoneNumber: String? = null,
    val status: String? = null,
    val socialAccounts: SocialAccountsDto? = null,
    val deviceToken: String? = null
)
