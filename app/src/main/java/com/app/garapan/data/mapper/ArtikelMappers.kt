package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ArtikelDto
import com.app.garapan.domain.model.Artikel

fun ArtikelDto.toDomain() = Artikel(
    id = id,
    title = title,
    content = content,
    imageUrl = imageUrl,
    publishedAt = publishedAt
)
