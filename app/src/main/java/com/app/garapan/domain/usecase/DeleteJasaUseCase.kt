package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.JasaRepository
import javax.inject.Inject

class DeleteJasaUseCase @Inject constructor(
    private val jasaRepository: JasaRepository
) {
    suspend operator fun invoke(id: String) = jasaRepository.deleteJasa(id)
}
