package com.app.garapan.domain.model

data class Artikel(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val publishedAt: String?
)
