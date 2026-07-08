package com.app.garapan.data.remote.dto

data class PesananDto(
    val id: String,
    val conversationId: String? = null,
    val klienId: String,
    val mahasiswaId: String,
    val jasaId: String? = null,
    val projectId: String? = null,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val jasa: PesananJasaDto? = null,
    val project: PesananProjectDto? = null,
    val klien: PesananKlienDto? = null,
    val mahasiswa: PesananMahasiswaDto? = null,
    val pembayaran: PesananPembayaranDto? = null,
    val laporan: List<LaporanDto>? = null
)

data class PesananJasaDto(
    val id: String,
    val title: String
)

data class PesananProjectDto(
    val id: String,
    val title: String,
    val deadline: String? = null,
    val imageUrl: String? = null
)

data class PesananKlienDto(
    val id: String,
    val companyName: String? = null,
    val user: PesananUserDto? = null
)

data class PesananMahasiswaDto(
    val id: String,
    val fullName: String? = null,
    val university: String? = null,
    val user: PesananUserDto? = null
)

data class PesananUserDto(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val displayName: String? = null
)

data class PesananPembayaranDto(
    val id: String,
    val status: String,
    val method: String? = null,
    val paidAt: String? = null
)

data class PesananListResponseDto(
    val data: List<PesananDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class CreatePesananRequest(
    val jasaId: String
)

data class LaporanDto(
    val id: String,
    val reporterId: String,
    val reason: String,
    val status: String,
    val resolutionNote: String? = null,
    val refundAmount: Double? = null,
    val createdAt: String,
    val resolvedAt: String? = null
)
