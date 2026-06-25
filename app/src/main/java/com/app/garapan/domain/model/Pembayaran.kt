package com.app.garapan.domain.model

data class Pembayaran(
    val id: String,
    val pesananId: String,
    val amount: Double,
    val method: PaymentMethod,
    val midtransToken: String,
    val status: PaymentStatus,
    val paidAt: String?
)
