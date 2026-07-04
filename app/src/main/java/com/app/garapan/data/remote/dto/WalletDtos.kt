package com.app.garapan.data.remote.dto

data class WalletSummaryDto(
    val balance: String,
    val currency: String,
    val incomeThisMonth: String,
    val totalWithdrawn: String,
    val heldInEscrow: String,
    val availableForWithdraw: String
)

data class WalletTransactionDto(
    val id: String,
    val pesananId: String? = null,
    val title: String,
    val counterpartyName: String,
    val type: String,
    val direction: String,
    val amount: String,
    val status: String,
    val orderStatus: String? = null,
    val createdAt: String
)

data class WalletTransactionPageDto(
    val data: List<WalletTransactionDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class WithdrawalRequestDto(
    val amount: String,
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val note: String? = null
)

data class WithdrawalDto(
    val id: String,
    val mahasiswaId: String,
    val amount: String,
    val status: String,
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val note: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val paidAt: String? = null
)

data class WithdrawalPageDto(
    val data: List<WithdrawalDto>,
    val total: Int,
    val page: Int,
    val limit: Int
)
