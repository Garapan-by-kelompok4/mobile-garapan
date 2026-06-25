package com.app.garapan.domain.model

data class PublicProfile(
    val userId: String,
    val role: Role,
    val displayName: String,
    val avatarUrl: String?,
    val university: String?,
    val companyName: String?,
    val bio: String,
    val skills: List<ProfileSkill>,
    val rating: Float?,
    val mahasiswaId: String?
)
