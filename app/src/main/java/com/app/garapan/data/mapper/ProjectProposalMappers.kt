package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.ProjectProposalDto
import com.app.garapan.data.remote.dto.SubmitProposalRequestDto
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.ProposalStatus
import java.util.Locale

fun ProjectProposalDto.toDomain(): ProjectProposal = ProjectProposal(
    id = id,
    projectId = projectId,
    mahasiswaId = mahasiswaId,
    message = message,
    proposedPrice = proposedPrice.toDoubleOrNull() ?: 0.0,
    status = ProposalStatus.fromApiValue(status),
    createdAt = createdAt.orEmpty(),
    mahasiswaName = mahasiswa?.fullName?.takeIf { it.isNotBlank() }
        ?: mahasiswa?.user?.displayName
        ?: mahasiswa?.user?.name
        .orEmpty(),
    mahasiswaUniversity = mahasiswa?.university.orEmpty(),
    mahasiswaRating = mahasiswa?.rating ?: 0.0,
    projectTitle = project?.title.orEmpty(),
    projectBudget = project?.budget?.toDoubleOrNull() ?: 0.0,
    projectDeadline = project?.deadline.orEmpty(),
    projectStatus = project?.status?.let(ProjectStatus::fromApiValue) ?: ProjectStatus.OPEN,
    projectImageUrl = project?.imageUrl.orEmpty()
)

fun submitProposalRequest(message: String, proposedPrice: Double): SubmitProposalRequestDto =
    SubmitProposalRequestDto(
        message = message,
        proposedPrice = proposedPrice.toProposalPriceString()
    )

private fun Double.toProposalPriceString(): String =
    if (this % 1.0 == 0.0) {
        "${toLong()}.00"
    } else {
        String.format(Locale.US, "%.2f", this)
    }
