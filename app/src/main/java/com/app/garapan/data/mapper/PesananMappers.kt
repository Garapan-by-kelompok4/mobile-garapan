package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.CreatePesananRequest
import com.app.garapan.data.remote.dto.CreatePaymentTokenRequest
import com.app.garapan.data.remote.dto.PembayaranDto
import com.app.garapan.data.remote.dto.LaporanDto
import com.app.garapan.data.remote.dto.PesananDto
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.model.CreatePesananParams
import com.app.garapan.domain.model.PaymentMethod
import com.app.garapan.domain.model.PaymentStatus
import com.app.garapan.domain.model.Laporan
import com.app.garapan.domain.model.LaporanStatus
import com.app.garapan.domain.model.Pembayaran
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananPaymentSummary
import com.app.garapan.domain.model.PesananStatus

fun PesananDto.toDomain(): Pesanan = Pesanan(
    id = id,
    conversationId = conversationId,
    klienId = klienId,
    mahasiswaId = mahasiswaId,
    jasaId = jasaId,
    projectId = projectId,
    totalPrice = totalPrice,
    status = PesananStatus.fromApiValue(status),
    createdAt = createdAt,
    updatedAt = updatedAt,
    jasaTitle = jasa?.title.orEmpty(),
    projectTitle = project?.title.orEmpty(),
    workerName = mahasiswa.resolveDisplayName(),
    clientLabel = klien.resolveDisplayName(),
    workerUserId = mahasiswa?.user?.id,
    clientUserId = klien?.user?.id,
    payment = pembayaran?.toSummary(),
    laporan = laporan?.firstOrNull()?.toDomain()
)

fun LaporanDto.toDomain(): Laporan = Laporan(
    id = id,
    reporterId = reporterId,
    reason = reason,
    status = LaporanStatus.fromApiValue(status),
    resolutionNote = resolutionNote,
    refundAmount = refundAmount,
    createdAt = createdAt,
    resolvedAt = resolvedAt
)

private fun com.app.garapan.data.remote.dto.PesananMahasiswaDto?.resolveDisplayName(): String {
    if (this == null) return "Freelancer"
    return fullName?.takeIf { it.isNotBlank() }
        ?: user?.displayName
        ?: user?.name
        ?: user?.email
        ?: university
        ?: "Freelancer"
}

private fun com.app.garapan.data.remote.dto.PesananKlienDto?.resolveDisplayName(): String {
    if (this == null) return "Klien"
    return companyName?.takeIf { it.isNotBlank() }
        ?: user?.displayName
        ?: user?.name
        ?: user?.email
        ?: "Klien"
}

private fun com.app.garapan.data.remote.dto.PesananPembayaranDto.toSummary() = PesananPaymentSummary(
    id = id,
    status = PaymentStatus.fromApiValue(status),
    method = method?.let(PaymentMethod::fromApiValue),
    paidAt = paidAt
)

fun PembayaranDto.toDomain(): Pembayaran = Pembayaran(
    id = id,
    pesananId = pesananId,
    amount = amount,
    method = PaymentMethod.fromApiValue(method),
    midtransToken = midtransToken,
    status = PaymentStatus.fromApiValue(status),
    paidAt = paidAt
)

fun CreatePesananParams.toRequest(): CreatePesananRequest = CreatePesananRequest(jasaId = jasaId)

fun CreatePaymentTokenParams.toRequest(): CreatePaymentTokenRequest = CreatePaymentTokenRequest(
    pesananId = pesananId,
    method = method.toApiValue()
)
