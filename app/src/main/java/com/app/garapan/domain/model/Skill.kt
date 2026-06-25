package com.app.garapan.domain.model

data class Skill(
    val id: String,
    val name: String,
    val kategoriId: String? = null,
    val kategoriName: String = ""
)

data class ProfileSkill(
    val id: String,
    val name: String,
    val kategoriName: String = ""
)
