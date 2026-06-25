package com.app.garapan.data.remote.dto

data class SkillDto(
    val id: String,
    val name: String,
    val kategoriId: String? = null,
    val kategori: KategoriDto? = null
)

data class PublicSkillDto(
    val id: String? = null,
    val name: String,
    val kategoriId: String? = null,
    val kategori: KategoriDto? = null
)
