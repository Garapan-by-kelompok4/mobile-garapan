package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toDto
import com.app.garapan.data.remote.api.WalletApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.WalletSummary
import com.app.garapan.domain.model.WalletTransactionPage
import com.app.garapan.domain.model.Withdrawal
import com.app.garapan.domain.model.WithdrawalPage
import com.app.garapan.domain.model.WithdrawalRequest
import com.app.garapan.domain.repository.WalletRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val walletApi: WalletApi
) : WalletRepository {
    override suspend fun getWalletSummary(): Resource<WalletSummary> =
        safeApiCall { walletApi.getWalletSummary().toDomain() }

    override suspend fun getTransactions(
        page: Int,
        limit: Int,
        type: String?,
        status: String?
    ): Resource<WalletTransactionPage> =
        safeApiCall { walletApi.getTransactions(page, limit, type, status).toDomain() }

    override suspend fun requestWithdrawal(request: WithdrawalRequest): Resource<Withdrawal> =
        safeApiCall { walletApi.requestWithdrawal(request.toDto()).toDomain() }

    override suspend fun getWithdrawals(page: Int, limit: Int, status: String?): Resource<WithdrawalPage> =
        safeApiCall { walletApi.getWithdrawals(page, limit, status).toDomain() }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
