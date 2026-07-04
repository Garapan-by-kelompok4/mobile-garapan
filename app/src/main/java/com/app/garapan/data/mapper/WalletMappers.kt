package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.WalletSummaryDto
import com.app.garapan.data.remote.dto.WalletTransactionDto
import com.app.garapan.data.remote.dto.WalletTransactionPageDto
import com.app.garapan.data.remote.dto.WithdrawalDto
import com.app.garapan.data.remote.dto.WithdrawalPageDto
import com.app.garapan.data.remote.dto.WithdrawalRequestDto
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.domain.model.WalletSummary
import com.app.garapan.domain.model.WalletTransaction
import com.app.garapan.domain.model.WalletTransactionDirection
import com.app.garapan.domain.model.WalletTransactionPage
import com.app.garapan.domain.model.WalletTransactionStatus
import com.app.garapan.domain.model.WalletTransactionType
import com.app.garapan.domain.model.Withdrawal
import com.app.garapan.domain.model.WithdrawalPage
import com.app.garapan.domain.model.WithdrawalRequest

fun WalletSummaryDto.toDomain() = WalletSummary(
    balance = balance,
    currency = currency,
    incomeThisMonth = incomeThisMonth,
    totalWithdrawn = totalWithdrawn,
    heldInEscrow = heldInEscrow,
    availableForWithdraw = availableForWithdraw
)

fun WalletTransactionPageDto.toDomain() = WalletTransactionPage(
    data = data.map { it.toDomain() },
    total = total,
    page = page,
    limit = limit
)

fun WalletTransactionDto.toDomain() = WalletTransaction(
    id = id,
    pesananId = pesananId,
    title = title,
    counterpartyName = counterpartyName,
    type = type.toWalletTransactionType(),
    direction = direction.toWalletDirection(),
    amount = amount,
    status = status.toWalletStatus(),
    orderStatus = orderStatus?.let { PesananStatus.fromApiValue(it) },
    createdAt = createdAt
)

fun WithdrawalRequest.toDto() = WithdrawalRequestDto(
    amount = amount,
    bankName = bankName,
    accountNumber = accountNumber,
    accountHolderName = accountHolderName,
    note = note?.takeIf { it.isNotBlank() }
)

fun WithdrawalDto.toDomain() = Withdrawal(
    id = id,
    mahasiswaId = mahasiswaId,
    amount = amount,
    status = status.toWalletStatus(),
    bankName = bankName,
    accountNumber = accountNumber,
    accountHolderName = accountHolderName,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    paidAt = paidAt
)

fun WithdrawalPageDto.toDomain() = WithdrawalPage(
    data = data.map { it.toDomain() },
    total = total,
    page = page,
    limit = limit
)

private fun String.toWalletTransactionType(): WalletTransactionType =
    runCatching { WalletTransactionType.valueOf(uppercase()) }.getOrDefault(WalletTransactionType.UNKNOWN)

private fun String.toWalletDirection(): WalletTransactionDirection =
    runCatching { WalletTransactionDirection.valueOf(uppercase()) }.getOrDefault(WalletTransactionDirection.UNKNOWN)

private fun String.toWalletStatus(): WalletTransactionStatus =
    runCatching { WalletTransactionStatus.valueOf(uppercase()) }.getOrDefault(WalletTransactionStatus.UNKNOWN)
