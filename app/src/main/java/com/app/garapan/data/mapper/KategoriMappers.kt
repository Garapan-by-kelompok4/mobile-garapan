package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.KategoriDto
import com.app.garapan.domain.model.Kategori

fun KategoriDto.toDomain() = Kategori(
    id = id,
    name = name,
    icon = icon
)
