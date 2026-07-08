package com.app.garapan.domain.model

data class Pesanan(
    val id: String,
    val conversationId: String?,
    val klienId: String,
    val mahasiswaId: String,
    val jasaId: String?,
    val projectId: String?,
    val totalPrice: Double,
    val status: PesananStatus,
    val createdAt: String,
    val updatedAt: String,
    val jasaTitle: String,
    val projectTitle: String,
    val workerName: String,
    val clientLabel: String,
    val workerUserId: String?,
    val clientUserId: String?,
    val payment: PesananPaymentSummary?,
    val laporan: Laporan? = null
)

data class PesananPaymentSummary(
    val id: String,
    val status: PaymentStatus,
    val method: PaymentMethod?,
    val paidAt: String?
)

enum class PesananStatus {
    PENDING,
    PAID,
    IN_PROGRESS,
    DELIVERED,
    COMPLETED,
    DISPUTED,
    CANCELLED;

    companion object {
        fun fromApiValue(value: String): PesananStatus =
            entries.firstOrNull { it.name == value.uppercase() } ?: PENDING
    }
}

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED;

    companion object {
        fun fromApiValue(value: String): PaymentStatus =
            entries.firstOrNull { it.name == value.uppercase() } ?: PENDING
    }
}
