package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ArtikelRepository
import javax.inject.Inject

class GetArtikelRecommendationsUseCase @Inject constructor(
    private val artikelRepository: ArtikelRepository
) {
    suspend operator fun invoke(id: String, limit: Int = 2) =
        artikelRepository.getArtikelRecommendations(id = id, limit = limit)
}
