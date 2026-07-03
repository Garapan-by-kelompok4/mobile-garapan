package com.app.garapan.domain.model

data class Artikel(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val category: String?,
    val tags: List<String>,
    val seoDescription: String?,
    val views: Int,
    val publishedAt: String?,
    val status: String?,
    val author: ArtikelAuthor
)

data class ArtikelAuthor(
    val name: String,
    val role: String,
    val avatarUrl: String?
)

data class ArtikelRecommendation(
    val id: String,
    val title: String,
    val excerpt: String,
    val imageUrl: String?,
    val category: String?,
    val publishedAt: String?
)
