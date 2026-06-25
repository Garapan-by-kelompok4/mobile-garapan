package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.CreateProjectRequest
import com.app.garapan.data.remote.dto.ProjectDto
import com.app.garapan.data.remote.dto.UpdateProjectRequest
import com.app.garapan.domain.model.CreateProjectParams
import com.app.garapan.domain.model.Project
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.UpdateProjectParams
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

fun ProjectDto.toDomain(): Project = Project(
    id = id,
    klienId = klienId,
    kategoriId = kategoriId,
    assignedMahasiswaId = assignedMahasiswaId,
    title = title,
    description = description,
    budget = budget.toDoubleOrNull() ?: 0.0,
    deadline = deadline,
    status = ProjectStatus.fromApiValue(status),
    imageUrl = imageUrl.orEmpty(),
    kategoriName = kategori?.name.orEmpty(),
    clientName = klien.resolveDisplayName(),
    assigneeName = assignedMahasiswa.resolveDisplayName()
)

private fun com.app.garapan.data.remote.dto.ProjectKlienDto?.resolveDisplayName(): String {
    if (this == null) return ""
    return companyName?.takeIf { it.isNotBlank() }
        ?: user?.displayName
        ?: user?.name
        .orEmpty()
}

private fun com.app.garapan.data.remote.dto.ProjectMahasiswaDto?.resolveDisplayName(): String {
    if (this == null) return ""
    return fullName?.takeIf { it.isNotBlank() }
        ?: user?.displayName
        ?: user?.name
        .orEmpty()
}

fun CreateProjectParams.toRequest(): CreateProjectRequest = CreateProjectRequest(
    title = title,
    description = description,
    budget = budget.toProjectBudgetString(),
    deadline = deadline,
    kategoriId = kategoriId
)

fun UpdateProjectParams.toRequest(): UpdateProjectRequest = UpdateProjectRequest(
    title = title,
    description = description,
    budget = budget?.toProjectBudgetString(),
    deadline = deadline,
    kategoriId = kategoriId,
    status = status?.name
)

data class ProjectMultipartRequest(
    val kategoriId: RequestBody,
    val title: RequestBody,
    val description: RequestBody,
    val budget: RequestBody,
    val deadline: RequestBody,
    val image: MultipartBody.Part
)

fun CreateProjectParams.toMultipartRequest(): ProjectMultipartRequest {
    val imageBody = requireNotNull(image) { "image is required for multipart create" }
    val imagePart = MultipartBody.Part.createFormData(
        "image",
        imageBody.fileName,
        imageBody.bytes.toRequestBody(imageBody.mimeType.toMediaType())
    )
    return ProjectMultipartRequest(
        kategoriId = kategoriId.toRequestBody("text/plain".toMediaType()),
        title = title.toRequestBody("text/plain".toMediaType()),
        description = description.toRequestBody("text/plain".toMediaType()),
        budget = budget.toProjectBudgetString().toRequestBody("text/plain".toMediaType()),
        deadline = deadline.toRequestBody("text/plain".toMediaType()),
        image = imagePart
    )
}

fun UpdateProjectParams.toMultipartRequest(): Pair<ProjectMultipartFields, MultipartBody.Part?> {
    val imagePart = image?.let { imageBody ->
        MultipartBody.Part.createFormData(
            "image",
            imageBody.fileName,
            imageBody.bytes.toRequestBody(imageBody.mimeType.toMediaType())
        )
    }
    return ProjectMultipartFields(
        title = title?.toRequestBody("text/plain".toMediaType()),
        description = description?.toRequestBody("text/plain".toMediaType()),
        budget = budget?.toProjectBudgetString()?.toRequestBody("text/plain".toMediaType()),
        deadline = deadline?.toRequestBody("text/plain".toMediaType()),
        kategoriId = kategoriId?.toRequestBody("text/plain".toMediaType()),
        status = status?.name?.toRequestBody("text/plain".toMediaType())
    ) to imagePart
}

data class ProjectMultipartFields(
    val title: RequestBody? = null,
    val description: RequestBody? = null,
    val budget: RequestBody? = null,
    val deadline: RequestBody? = null,
    val kategoriId: RequestBody? = null,
    val status: RequestBody? = null
)

private fun Double.toProjectBudgetString(): String =
    if (this % 1.0 == 0.0) {
        "${toLong()}.00"
    } else {
        String.format(Locale.US, "%.2f", this)
    }
