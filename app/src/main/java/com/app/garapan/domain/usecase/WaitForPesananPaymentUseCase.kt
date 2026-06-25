package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Pesanan
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.repository.PesananRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class WaitForPesananPaymentUseCase @Inject constructor(
    private val pesananRepository: PesananRepository
) {
    suspend operator fun invoke(
        id: String,
        maxAttempts: Int = 15,
        delayMs: Long = 1_500L,
        initialDelayMs: Long = 1_000L
    ): Resource<Pesanan> {
        if (initialDelayMs > 0) delay(initialDelayMs)
        repeat(maxAttempts) { attempt ->
            when (val result = pesananRepository.getPesananDetail(id)) {
                is Resource.Success -> {
                    if (result.data.status != PesananStatus.PENDING) {
                        return result
                    }
                }
                is Resource.Error -> {
                    if (attempt == maxAttempts - 1) return result
                }
                Resource.Loading -> Unit
            }
            delay(delayMs)
        }
        return pesananRepository.getPesananDetail(id)
    }
}
