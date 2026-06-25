package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.CreatePaymentTokenRequest
import com.app.garapan.data.remote.dto.PembayaranDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PembayaranApi {
    @POST("pembayaran/create-token")
    suspend fun createPaymentToken(@Body body: CreatePaymentTokenRequest): PembayaranDto
}
