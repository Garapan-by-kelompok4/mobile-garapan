package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.JasaListFilters
import com.app.garapan.domain.repository.JasaRepository
import javax.inject.Inject

class GetMyJasaListUseCase @Inject constructor(
    private val jasaRepository: JasaRepository
) {
    suspend operator fun invoke(filters: JasaListFilters = JasaListFilters()) =
        jasaRepository.getMyJasaList(filters)
}
