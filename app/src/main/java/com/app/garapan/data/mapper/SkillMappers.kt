package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.PublicSkillDto
import com.app.garapan.data.remote.dto.SkillDto
import com.app.garapan.domain.model.ProfileSkill
import com.app.garapan.domain.model.Skill

fun SkillDto.toDomain() = Skill(
    id = id,
    name = name,
    kategoriId = kategoriId,
    kategoriName = kategori?.name.orEmpty()
)

fun PublicSkillDto.toDomain() = ProfileSkill(
    id = id?.takeIf { it.isNotBlank() } ?: "skill-${name.hashCode()}",
    name = name,
    kategoriName = kategori?.name.orEmpty()
)
