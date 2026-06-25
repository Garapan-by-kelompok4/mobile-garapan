package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.TopWorkerDto
import com.app.garapan.domain.model.TopWorker

fun TopWorkerDto.toDomain(): TopWorker = TopWorker(
    id = id,
    mahasiswaId = mahasiswaId,
    userId = userId,
    rank = rank,
    score = score,
    rating = rating.toFloat(),
    displayName = displayName.ifBlank { university }.ifBlank { "Mahasiswa" },
    avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
    university = university,
    skills = skills,
    completedOrders = completedOrders
)
