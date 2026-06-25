package com.app.garapan.domain.model

data class Project(
    val id: String,
    val klienId: String,
    val kategoriId: String,
    val title: String,
    val description: String,
    val budget: Double,
    val deadline: String,
    val status: String,
    val kategoriName: String = "",
    val clientName: String = ""
)

data class ProjectListFilters(
    val search: String? = null,
    val kategoriId: String? = null,
    val minBudget: Double? = null,
    val maxBudget: Double? = null,
    val page: Int = 1,
    val limit: Int = 20,
    val includeRelatedSkills: Boolean = false
)
