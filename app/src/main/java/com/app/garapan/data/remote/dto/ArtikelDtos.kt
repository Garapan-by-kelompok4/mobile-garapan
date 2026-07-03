package com.app.garapan.data.remote.dto

data class ArtikelDto(
    val id: String,
    val adminId: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val seoDescription: String? = null,
    val views: Int = 0,
    val publishedAt: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val status: String? = null,
    val author: ArtikelAuthorDto? = null
)

data class ArtikelListResponseDto(
    val data: List<ArtikelDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class ArtikelAuthorDto(
    val name: String?,
    val role: String?,
    val avatarUrl: String?
)

data class ArtikelRecommendationDto(
    val id: String,
    val title: String,
    val excerpt: String,
    val imageUrl: String?,
    val category: String? = null,
    val publishedAt: String?
)

data class ArtikelRecommendationsResponseDto(
    val data: List<ArtikelRecommendationDto>,
    val total: Int,
    val limit: Int
)
