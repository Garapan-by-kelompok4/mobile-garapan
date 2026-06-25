package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.PublicKlienDto
import com.app.garapan.data.remote.dto.PublicMahasiswaDto
import com.app.garapan.data.remote.dto.PublicUserDto
import com.app.garapan.domain.model.PublicProfile
import com.app.garapan.domain.model.Role

fun PublicUserDto.toPublicProfile(): PublicProfile {
    mahasiswa?.let { return it.toPublicProfile(userId = id, role = role) }
    klien?.let { return it.toPublicProfile(userId = id, role = role) }
    return PublicProfile(
        userId = id,
        role = Role.valueOf(role),
        displayName = "Pengguna",
        avatarUrl = null,
        university = null,
        companyName = null,
        bio = "",
        skills = emptyList(),
        rating = null,
        mahasiswaId = null
    )
}

private fun PublicMahasiswaDto.toPublicProfile(userId: String, role: String) = PublicProfile(
    userId = userId,
    role = Role.valueOf(role),
    displayName = fullName?.trim().takeUnless { it.isNullOrBlank() } ?: "Mahasiswa",
    avatarUrl = avatarUrl?.takeIf { it.isNotBlank() },
    university = university,
    companyName = null,
    bio = bio,
    skills = skills.map { it.toDomain() },
    rating = rating.toFloat(),
    mahasiswaId = id
)

private fun PublicKlienDto.toPublicProfile(userId: String, role: String) = PublicProfile(
    userId = userId,
    role = Role.valueOf(role),
    displayName = companyName?.trim().takeUnless { it.isNullOrBlank() } ?: "Klien",
    avatarUrl = null,
    university = null,
    companyName = companyName,
    bio = bio,
    skills = emptyList(),
    rating = null,
    mahasiswaId = null
)
