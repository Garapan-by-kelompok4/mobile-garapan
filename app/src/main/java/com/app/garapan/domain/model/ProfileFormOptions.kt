package com.app.garapan.domain.model

/**
 * Shared profile form options and bio helpers used by setup and edit-profile flows.
 */
object ProfileFormOptions {
    val yearsOfExperience = listOf("0-1 years", "1-3 years", "3-5 years", "5+ years")
    val industries = listOf("Technology", "Finance", "Education", "Healthcare", "Retail", "Other")
    val statusOptions: List<ProfileStatus> = ProfileStatus.entries
}

data class MahasiswaBioParts(
    val major: String = "",
    val yearsOfExperience: String = ""
)

fun buildMahasiswaBio(major: String, yearsOfExperience: String): String =
    listOf(major.trim(), yearsOfExperience.trim())
        .filter { it.isNotBlank() }
        .joinToString(separator = " | ")

fun parseMahasiswaBio(bio: String): MahasiswaBioParts {
    val parts = bio.split(" | ").map { it.trim() }.filter { it.isNotBlank() }
    return when (parts.size) {
        0 -> MahasiswaBioParts()
        1 -> MahasiswaBioParts(major = parts[0])
        else -> MahasiswaBioParts(major = parts[0], yearsOfExperience = parts[1])
    }
}

fun buildClientBio(industry: String, services: Collection<String>): String {
    val parts = buildList {
        industry.trim().takeIf { it.isNotBlank() }?.let(::add)
        if (services.isNotEmpty()) add(services.joinToString(", "))
    }
    return parts.joinToString(separator = " | ")
}

fun parseClientBio(bio: String): Pair<String, Set<String>> {
    val parts = bio.split(" | ").map { it.trim() }.filter { it.isNotBlank() }
    if (parts.isEmpty()) return "" to emptySet()
    val industry = parts[0]
    val services = parts.getOrNull(1)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.toSet()
        .orEmpty()
    return industry to services
}
