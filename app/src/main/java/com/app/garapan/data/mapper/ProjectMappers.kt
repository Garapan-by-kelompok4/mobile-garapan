package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ProjectDto
import com.app.garapan.domain.model.Project

fun ProjectDto.toDomain(): Project = Project(
    id = id,
    klienId = klienId,
    kategoriId = kategoriId,
    title = title,
    description = description,
    budget = budget.toDoubleOrNull() ?: 0.0,
    deadline = deadline,
    status = status,
    kategoriName = kategori?.name.orEmpty(),
    clientName = klien?.companyName.orEmpty()
)
