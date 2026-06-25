package com.app.garapan.data.remote.dto

data class PortofolioDto(
    val id: String,
    val mahasiswaId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val projectUrl: String?
)
