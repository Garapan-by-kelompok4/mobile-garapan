package com.app.garapan.domain.model

data class UpdateProfileParams(
    val displayName: String? = null,
    val university: String? = null,
    val skills: List<String>? = null,
    val bio: String? = null,
    val companyName: String? = null,
    val phoneNumber: String? = null,
    val status: ProfileStatus? = null,
    val linkedinUrl: String? = null,
    val deviceToken: String? = null
)
