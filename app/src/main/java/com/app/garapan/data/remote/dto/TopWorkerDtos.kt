package com.app.garapan.data.remote.dto

data class TopWorkerDto(
    val id: String,
    val mahasiswaId: String,
    val userId: String = "",
    val score: Double,
    val period: String,
    val rank: Int,
    val completedOrders: Int = 0,
    val skills: List<String> = emptyList(),
    val displayName: String = "",
    val avatarUrl: String? = null,
    val university: String = "",
    val rating: Double = 0.0
)
