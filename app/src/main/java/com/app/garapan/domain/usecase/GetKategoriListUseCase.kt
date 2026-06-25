package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.KategoriRepository
import javax.inject.Inject

class GetKategoriListUseCase @Inject constructor(
    private val kategoriRepository: KategoriRepository
) {
    suspend operator fun invoke() = kategoriRepository.getKategoriList()
}
