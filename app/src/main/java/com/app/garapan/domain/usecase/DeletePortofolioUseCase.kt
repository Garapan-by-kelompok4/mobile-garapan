package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.repository.PortofolioRepository
import javax.inject.Inject

class DeletePortofolioUseCase @Inject constructor(
    private val portofolioRepository: PortofolioRepository
) {
    suspend operator fun invoke(id: String): Resource<Unit> =
        portofolioRepository.deletePortofolio(id)
}
