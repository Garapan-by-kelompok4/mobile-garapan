package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.CreateJasaParams
import com.app.garapan.domain.repository.JasaRepository
import javax.inject.Inject

class CreateJasaUseCase @Inject constructor(
    private val jasaRepository: JasaRepository
) {
    suspend operator fun invoke(params: CreateJasaParams) = jasaRepository.createJasa(params)
}
