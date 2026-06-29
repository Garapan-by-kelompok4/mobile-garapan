package com.app.garapan.domain.model

data class User(
    val id: String,
    val email: String,
    val role: Role,
    val emailVerified: Boolean,
    val deviceToken: String?,
    val twoFactorEnabled: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val mahasiswa: MahasiswaProfile?,
    val klien: KlienProfile?,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val status: ProfileStatus? = null,
    val socialAccounts: SocialAccounts = SocialAccounts()
) {
    /** Avatar URL regardless of role; null when no avatar is set. */
    val avatarUrl: String?
        get() = mahasiswa?.avatarUrl ?: klien?.avatarUrl
}

data class MahasiswaProfile(
    val id: String,
    val userId: String,
    val university: String,
    val skills: List<String>,
    val bio: String,
    val walletBalance: String,
    val rating: Double,
    val suggestedKategoriId: String? = null,
    val suggestedKategoriName: String = "",
    val fullName: String? = null,
    val avatarUrl: String? = null
)

data class KlienProfile(
    val id: String,
    val userId: String,
    val companyName: String?,
    val bio: String,
    val walletBalance: String,
    val avatarUrl: String? = null
)
