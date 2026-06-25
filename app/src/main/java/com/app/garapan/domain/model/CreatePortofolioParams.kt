package com.app.garapan.domain.model

data class CreatePortofolioParams(
    val title: String,
    val description: String,
    val image: PortofolioImage,
    val projectUrl: String?
)
