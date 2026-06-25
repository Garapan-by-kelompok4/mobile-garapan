package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.repository.PesananRepository
import javax.inject.Inject

class GetMyPesananUseCase @Inject constructor(
    private val pesananRepository: PesananRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 20): Resource<List<Pesanan>> =
        pesananRepository.getMyPesananList(page = page, limit = limit)
}
