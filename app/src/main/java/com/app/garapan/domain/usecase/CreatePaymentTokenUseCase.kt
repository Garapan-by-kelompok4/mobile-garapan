package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.model.Pembayaran
import com.app.garapan.domain.repository.PembayaranRepository
import javax.inject.Inject

class CreatePaymentTokenUseCase @Inject constructor(
    private val pembayaranRepository: PembayaranRepository
) {
    suspend operator fun invoke(params: CreatePaymentTokenParams): Resource<Pembayaran> =
        pembayaranRepository.createPaymentToken(params)
}
