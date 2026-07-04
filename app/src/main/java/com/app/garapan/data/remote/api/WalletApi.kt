package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.WalletSummaryDto
import com.app.garapan.data.remote.dto.WalletTransactionPageDto
import com.app.garapan.data.remote.dto.WithdrawalDto
import com.app.garapan.data.remote.dto.WithdrawalPageDto
import com.app.garapan.data.remote.dto.WithdrawalRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WalletApi {
    @GET("wallet/me")
    suspend fun getWalletSummary(): WalletSummaryDto

    @GET("wallet/me/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null
    ): WalletTransactionPageDto

    @POST("wallet/me/withdrawals")
    suspend fun requestWithdrawal(@Body body: WithdrawalRequestDto): WithdrawalDto

    @GET("wallet/me/withdrawals")
    suspend fun getWithdrawals(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null
    ): WithdrawalPageDto
}
