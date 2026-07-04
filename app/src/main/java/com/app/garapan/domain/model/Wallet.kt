package com.app.garapan.domain.model

data class WalletSummary(
    val balance: String,
    val currency: String,
    val incomeThisMonth: String,
    val totalWithdrawn: String,
    val heldInEscrow: String,
    val availableForWithdraw: String
)

data class WalletTransactionPage(
    val data: List<WalletTransaction>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class WalletTransaction(
    val id: String,
    val pesananId: String?,
    val title: String,
    val counterpartyName: String,
    val type: WalletTransactionType,
    val direction: WalletTransactionDirection,
    val amount: String,
    val status: WalletTransactionStatus,
    val createdAt: String
)

enum class WalletTransactionType {
    CREDIT,
    REFUND,
    ESCROW,
    WITHDRAWAL,
    UNKNOWN
}

enum class WalletTransactionDirection {
    IN,
    OUT,
    UNKNOWN
}

enum class WalletTransactionStatus {
    SUCCESS,
    PENDING,
    APPROVED,
    REJECTED,
    PAID,
    CANCELLED,
    UNKNOWN
}

data class WithdrawalRequest(
    val amount: String,
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val note: String? = null
)

data class Withdrawal(
    val id: String,
    val mahasiswaId: String,
    val amount: String,
    val status: WalletTransactionStatus,
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val note: String?,
    val createdAt: String,
    val updatedAt: String,
    val paidAt: String?
)

data class WithdrawalPage(
    val data: List<Withdrawal>,
    val total: Int,
    val page: Int,
    val limit: Int
)
