package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.ArtikelRepository
import javax.inject.Inject

class GetArtikelDetailUseCase @Inject constructor(
    private val artikelRepository: ArtikelRepository
) {
    suspend operator fun invoke(id: String) = artikelRepository.getArtikelDetail(id)
}
