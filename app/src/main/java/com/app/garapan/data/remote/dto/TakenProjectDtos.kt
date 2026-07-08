package com.app.garapan.data.remote.dto

data class TakenProjectListResponseDto(
    val data: List<TakenProjectDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class TakenProjectDto(
    val projectId: String,
    val title: String,
    val budget: String,
    val deadline: String,
    val imageUrl: String? = null,
    val kategoriName: String? = null,
    val clientName: String? = null,
    val proposalId: String,
    val proposedPrice: String,
    val acceptedAt: String? = null,
    val pesanan: TakenProjectPesananDto? = null
)

data class TakenProjectPesananDto(
    val id: String,
    val status: String,
    val updatedAt: String
)
