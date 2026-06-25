package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.JasaDto
import com.app.garapan.data.remote.dto.JasaPortofolioPreviewDto
import com.app.garapan.data.remote.dto.UpdateJasaRequest
import com.app.garapan.domain.model.CreateJasaParams
import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.JasaPortfolioPreview
import com.app.garapan.domain.model.JasaStatus
import com.app.garapan.domain.model.UpdateJasaParams
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun JasaDto.toDomain(): Jasa = Jasa(
    id = id,
    mahasiswaId = mahasiswaId,
    kategoriId = kategoriId,
    title = title,
    description = description,
    price = price,
    imageUrl = imageUrl,
    status = JasaStatus.fromApiValue(status),
    kategoriName = kategori?.name.orEmpty(),
    workerName = resolveWorkerName(),
    workerUserId = mahasiswa?.user?.id.orEmpty(),
    workerUniversity = mahasiswa?.university.orEmpty(),
    workerAvatarUrl = mahasiswa?.avatarUrl.orEmpty(),
    workerRating = mahasiswa?.rating ?: 0.0,
    rating = rating ?: 0.0,
    reviewCount = reviewCount ?: 0,
    portfolios = portofolio.orEmpty().map { it.toDomain() }
)

private fun JasaDto.resolveWorkerName(): String =
    mahasiswa?.fullName
        ?: mahasiswa?.user?.displayName
        ?: mahasiswa?.user?.name
        .orEmpty()

private fun JasaPortofolioPreviewDto.toDomain() = JasaPortfolioPreview(
    id = id,
    title = title,
    imageUrl = imageUrl,
    projectUrl = projectUrl
)

data class JasaMultipartRequest(
    val kategoriId: RequestBody,
    val title: RequestBody,
    val description: RequestBody,
    val price: RequestBody,
    val image: MultipartBody.Part
)

fun CreateJasaParams.toMultipartRequest(): JasaMultipartRequest {
    val imageBody = image.bytes.toRequestBody(image.mimeType.toMediaType())
    return JasaMultipartRequest(
        kategoriId = kategoriId.toRequestBody("text/plain".toMediaType()),
        title = title.toRequestBody("text/plain".toMediaType()),
        description = description.toRequestBody("text/plain".toMediaType()),
        price = price.toString().toRequestBody("text/plain".toMediaType()),
        image = MultipartBody.Part.createFormData("image", image.fileName, imageBody)
    )
}

fun UpdateJasaParams.toRequest(): UpdateJasaRequest = UpdateJasaRequest(
    title = title,
    description = description,
    price = price?.toJasaPriceString(),
    kategoriId = kategoriId,
    status = status?.name
)

private fun Double.toJasaPriceString(): String =
    if (this % 1.0 == 0.0) {
        toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", this)
    }
