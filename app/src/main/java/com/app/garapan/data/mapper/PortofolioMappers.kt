package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.PortofolioDto
import com.app.garapan.domain.model.CreatePortofolioParams
import com.app.garapan.domain.model.Portofolio
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun PortofolioDto.toDomain(): Portofolio = Portofolio(
    id = id,
    mahasiswaId = mahasiswaId,
    title = title,
    description = description,
    imageUrl = imageUrl,
    projectUrl = projectUrl
)

data class PortofolioMultipartRequest(
    val title: RequestBody,
    val description: RequestBody,
    val image: MultipartBody.Part,
    val projectUrl: RequestBody?
)

fun CreatePortofolioParams.toMultipartRequest(): PortofolioMultipartRequest {
    val imageBody = image.bytes.toRequestBody(image.mimeType.toMediaType())
    return PortofolioMultipartRequest(
        title = title.toRequestBody("text/plain".toMediaType()),
        description = description.toRequestBody("text/plain".toMediaType()),
        image = MultipartBody.Part.createFormData("image", image.fileName, imageBody),
        projectUrl = projectUrl?.toRequestBody("text/plain".toMediaType())
    )
}
