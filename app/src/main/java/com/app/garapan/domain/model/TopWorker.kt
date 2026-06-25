package com.app.garapan.domain.model

data class TopWorker(
    val id: String,
    val mahasiswaId: String,
    val userId: String,
    val rank: Int,
    val score: Double,
    val rating: Float,
    val displayName: String,
    val avatarUrl: String?,
    val university: String,
    val skills: List<String>,
    val completedOrders: Int
)
