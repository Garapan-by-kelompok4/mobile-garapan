package com.app.garapan.data.remote.dto

data class CreatePaymentTokenRequest(
    val pesananId: String,
    val method: String
)

data class PembayaranDto(
    val id: String,
    val pesananId: String,
    val amount: Double,
    val method: String,
    val midtransToken: String,
    val status: String,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
