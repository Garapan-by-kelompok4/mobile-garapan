package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.JasaRepository
import javax.inject.Inject

class GetJasaDetailUseCase @Inject constructor(
    private val jasaRepository: JasaRepository
) {
    suspend operator fun invoke(id: String) = jasaRepository.getJasaDetail(id)
}
