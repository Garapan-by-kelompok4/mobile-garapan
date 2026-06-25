package com.app.garapan.data.remote.dto

data class ArtikelDto(
    val id: String,
    val adminId: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val publishedAt: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ArtikelListResponseDto(
    val data: List<ArtikelDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)
