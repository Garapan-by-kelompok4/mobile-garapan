package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.TakenProjectDto
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.TakenProject

fun TakenProjectDto.toDomain(): TakenProject {
    val agreedBudget = proposedPrice.toDoubleOrNull() ?: budget.toDoubleOrNull() ?: 0.0
    return TakenProject(
        project = Project(
            id = projectId,
            klienId = "",
            kategoriId = "",
            title = title,
            description = "",
            budget = agreedBudget,
            deadline = deadline,
            status = ProjectStatus.OPEN,
            imageUrl = imageUrl.orEmpty(),
            kategoriName = kategoriName.orEmpty(),
            clientName = clientName.orEmpty()
        ),
        orderId = pesanan?.id,
        orderStatus = pesanan?.status?.let(PesananStatus::fromApiValue)
    )
}
