package com.app.garapan.domain.model

data class UpdateProfileParams(
    val university: String? = null,
    val skills: List<String>? = null,
    val bio: String? = null,
    val companyName: String? = null,
    val deviceToken: String? = null
)
