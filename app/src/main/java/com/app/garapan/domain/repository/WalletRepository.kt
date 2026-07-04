package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.WalletSummary
import com.app.garapan.domain.model.WalletTransactionPage
import com.app.garapan.domain.model.Withdrawal
import com.app.garapan.domain.model.WithdrawalPage
import com.app.garapan.domain.model.WithdrawalRequest

interface WalletRepository {
    suspend fun getWalletSummary(): Resource<WalletSummary>

    suspend fun getTransactions(
        page: Int = 1,
        limit: Int = 20,
        type: String? = null,
        status: String? = null
    ): Resource<WalletTransactionPage>

    suspend fun requestWithdrawal(request: WithdrawalRequest): Resource<Withdrawal>

    suspend fun getWithdrawals(
        page: Int = 1,
        limit: Int = 20,
        status: String? = null
    ): Resource<WithdrawalPage>
}
