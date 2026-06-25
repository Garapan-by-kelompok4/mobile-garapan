package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.CreatePaymentTokenParams
import com.app.garapan.domain.model.Pembayaran

interface PembayaranRepository {
    suspend fun createPaymentToken(params: CreatePaymentTokenParams): Resource<Pembayaran>
}
