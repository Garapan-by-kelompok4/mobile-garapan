package com.app.garapan.domain.usecase

import com.app.garapan.domain.repository.TopWorkerRepository
import javax.inject.Inject

class GetTopWorkersUseCase @Inject constructor(
    private val topWorkerRepository: TopWorkerRepository
) {
    suspend operator fun invoke() = topWorkerRepository.getTopWorkers()
}
