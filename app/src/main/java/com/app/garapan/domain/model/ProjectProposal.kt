package com.app.garapan.domain.model

data class ProjectProposal(
    val id: String,
    val projectId: String,
    val mahasiswaId: String,
    val message: String,
    val proposedPrice: Double,
    val status: ProposalStatus,
    val createdAt: String = "",
    val mahasiswaName: String = "",
    val mahasiswaUniversity: String = "",
    val mahasiswaRating: Double = 0.0,
    val projectTitle: String = "",
    val projectBudget: Double = 0.0,
    val projectDeadline: String = "",
    val projectStatus: ProjectStatus = ProjectStatus.OPEN,
    val projectImageUrl: String = ""
)

enum class ProposalStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    companion object {
        fun fromApiValue(value: String): ProposalStatus =
            entries.firstOrNull { it.name == value.uppercase() } ?: PENDING
    }
}

fun ProjectProposal.toTakenProject(): Project {
    val agreedBudget = proposedPrice.takeIf { it > 0.0 } ?: projectBudget
    return Project(
        id = projectId,
        klienId = "",
        kategoriId = "",
        title = projectTitle.ifBlank { "Proyek" },
        description = "",
        budget = agreedBudget,
        deadline = projectDeadline,
        status = projectStatus,
        imageUrl = projectImageUrl
    )
}
