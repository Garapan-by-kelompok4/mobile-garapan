package com.app.garapan.domain.model

data class CreatePaymentTokenParams(
    val pesananId: String,
    val method: PaymentMethod = PaymentMethod.GOPAY
)
