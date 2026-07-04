package com.app.garapan.domain.usecase

import com.app.garapan.domain.model.WithdrawalRequest
import com.app.garapan.domain.repository.WalletRepository
import javax.inject.Inject

class GetWalletSummaryUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke() = walletRepository.getWalletSummary()
}

class GetWalletTransactionsUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        type: String? = null,
        status: String? = null
    ) = walletRepository.getTransactions(page, limit, type, status)
}

class RequestWithdrawalUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(request: WithdrawalRequest) = walletRepository.requestWithdrawal(request)
}

class GetWithdrawalsUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        status: String? = null
    ) = walletRepository.getWithdrawals(page, limit, status)
}
