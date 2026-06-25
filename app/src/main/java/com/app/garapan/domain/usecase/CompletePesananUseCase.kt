package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.repository.PesananRepository
import javax.inject.Inject

class CompletePesananUseCase @Inject constructor(
    private val pesananRepository: PesananRepository
) {
    suspend operator fun invoke(id: String): Resource<Pesanan> =
        pesananRepository.completePesanan(id)
}
