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
    val klien: KlienProfile?
)

data class MahasiswaProfile(
    val id: String,
    val userId: String,
    val university: String,
    val skills: List<String>,
    val bio: String,
    val walletBalance: String,
    val rating: Double
)

data class KlienProfile(
    val id: String,
    val userId: String,
    val companyName: String?,
    val bio: String,
    val walletBalance: String
)
