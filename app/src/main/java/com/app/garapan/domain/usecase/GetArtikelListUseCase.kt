package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ArtikelRepository
import javax.inject.Inject

class GetArtikelListUseCase @Inject constructor(
    private val artikelRepository: ArtikelRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 20) =
        artikelRepository.getArtikelList(page = page, limit = limit)
}
