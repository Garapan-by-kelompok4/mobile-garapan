package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.repository.JasaRepository
import javax.inject.Inject

class GetJasaListUseCase @Inject constructor(
    private val jasaRepository: JasaRepository
) {
    suspend operator fun invoke(filters: JasaListFilters = JasaListFilters()) =
        jasaRepository.getJasaList(filters)
}
