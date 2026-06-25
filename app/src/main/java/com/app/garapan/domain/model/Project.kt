package com.app.garapan.domain.model

data class Project(
    val id: String,
    val klienId: String,
    val kategoriId: String,
    val assignedMahasiswaId: String? = null,
    val title: String,
    val description: String,
    val budget: Double,
    val deadline: String,
    val status: ProjectStatus,
    val imageUrl: String = "",
    val kategoriName: String = "",
    val clientName: String = "",
    val assigneeName: String = ""
)

enum class ProjectStatus {
    OPEN,
    CLOSED;

    companion object {
        fun fromApiValue(value: String): ProjectStatus =
            entries.firstOrNull { it.name == value.uppercase() } ?: OPEN
    }
}

data class ProjectListFilters(
    val search: String? = null,
    val kategoriId: String? = null,
    val minBudget: Double? = null,
    val maxBudget: Double? = null,
    val page: Int = 1,
    val limit: Int = 20,
    val includeRelatedSkills: Boolean = false
)

data class CreateProjectParams(
    val title: String,
    val description: String,
    val budget: Double,
    val deadline: String,
    val kategoriId: String,
    val image: PortofolioImage? = null
)

data class UpdateProjectParams(
    val title: String? = null,
    val description: String? = null,
    val budget: Double? = null,
    val deadline: String? = null,
    val kategoriId: String? = null,
    val status: ProjectStatus? = null,
    val image: PortofolioImage? = null
)
