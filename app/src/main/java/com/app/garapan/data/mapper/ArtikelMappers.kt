package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ArtikelDto
import com.app.garapan.data.remote.dto.ArtikelRecommendationDto
import com.app.garapan.domain.model.Artikel
import com.app.garapan.domain.model.ArtikelAuthor
import com.app.garapan.domain.model.ArtikelRecommendation

fun ArtikelDto.toDomain() = Artikel(
    id = id,
    title = title,
    content = content,
    imageUrl = imageUrl,
    category = category,
    tags = tags,
    seoDescription = seoDescription,
    views = views,
    publishedAt = publishedAt,
    status = status,
    author = ArtikelAuthor(
        name = author?.name?.takeIf { it.isNotBlank() } ?: DEFAULT_AUTHOR_NAME,
        role = author?.role?.takeIf { it.isNotBlank() } ?: DEFAULT_AUTHOR_ROLE,
        avatarUrl = author?.avatarUrl
    )
)

fun ArtikelRecommendationDto.toDomain() = ArtikelRecommendation(
    id = id,
    title = title,
    excerpt = excerpt,
    imageUrl = imageUrl,
    category = category,
    publishedAt = publishedAt
)

private const val DEFAULT_AUTHOR_NAME = "Admin GARAPAN"
private const val DEFAULT_AUTHOR_ROLE = "Editor"
