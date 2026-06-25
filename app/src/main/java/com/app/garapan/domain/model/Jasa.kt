package com.app.garapan.domain.model

data class Jasa(
    val id: String,
    val mahasiswaId: String,
    val kategoriId: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val status: JasaStatus,
    val kategoriName: String = "",
    val workerName: String = "",
    val workerUserId: String = "",
    val workerUniversity: String = "",
    val workerAvatarUrl: String = "",
    val workerRating: Double = 0.0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val portfolios: List<JasaPortfolioPreview> = emptyList()
)

data class JasaPortfolioPreview(
    val id: String,
    val title: String,
    val imageUrl: String,
    val projectUrl: String? = null
)

data class JasaListFilters(
    val search: String? = null,
    val kategoriId: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sort: String? = null,
    val mahasiswaId: String? = null,
    val page: Int = 1,
    val limit: Int = 20,
    val includeRelatedSkills: Boolean = false
)

data class CreateJasaParams(
    val kategoriId: String,
    val title: String,
    val description: String,
    val price: Double,
    val image: PortofolioImage
)

data class UpdateJasaParams(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val kategoriId: String? = null,
    val status: JasaStatus? = null
)
