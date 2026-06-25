package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.UpdateJasaParams
import com.app.garapan.domain.repository.JasaRepository
import javax.inject.Inject

class UpdateJasaUseCase @Inject constructor(
    private val jasaRepository: JasaRepository
) {
    suspend operator fun invoke(id: String, params: UpdateJasaParams) =
        jasaRepository.updateJasa(id, params)
}
