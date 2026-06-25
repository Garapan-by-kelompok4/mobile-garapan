package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.repository.LaporanRepository
import javax.inject.Inject

class SubmitDisputeUseCase @Inject constructor(
    private val repository: LaporanRepository
) {
    suspend operator fun invoke(pesananId: String, reason: String): Resource<Unit> =
        repository.createLaporan(pesananId, reason)
}
