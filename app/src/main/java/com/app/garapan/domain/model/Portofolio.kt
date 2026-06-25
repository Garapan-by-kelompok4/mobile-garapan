package com.app.garapan.domain.model

data class Portofolio(
    val id: String,
    val mahasiswaId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val projectUrl: String?
)
