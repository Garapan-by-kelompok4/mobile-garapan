package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePortofolioParams
import com.app.garapan.domain.model.Portofolio
import com.app.garapan.domain.repository.PortofolioRepository
import javax.inject.Inject

class AddPortofolioUseCase @Inject constructor(
    private val portofolioRepository: PortofolioRepository
) {
    suspend operator fun invoke(params: CreatePortofolioParams): Resource<Portofolio> =
        portofolioRepository.addPortofolio(params)
}
