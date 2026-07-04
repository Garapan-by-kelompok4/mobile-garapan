package com.app.garapan.domain.model

data class Laporan(
    val id: String,
    val reporterId: String,
    val reason: String,
    val status: LaporanStatus,
    val resolutionNote: String?,
    val refundAmount: Double?,
    val createdAt: String,
    val resolvedAt: String?
)

enum class LaporanStatus {
    PENDING,
    RESOLVED,
    REJECTED;

    companion object {
        fun fromApiValue(value: String): LaporanStatus =
            entries.firstOrNull { it.name == value.uppercase() } ?: PENDING
    }
}

enum class DisputeOutcome {
    RELEASE,
    REFUND,
    PARTIAL_REFUND,
    REJECT
}

object DisputeOutcomeResolver {
    fun resolveOutcome(
        laporanStatus: LaporanStatus?,
        orderStatus: PesananStatus,
        refundAmount: Double?
    ): DisputeOutcome? {
        return when (laporanStatus) {
            LaporanStatus.PENDING, null -> null
            LaporanStatus.REJECTED -> DisputeOutcome.REJECT
            LaporanStatus.RESOLVED -> when {
                orderStatus == PesananStatus.CANCELLED -> DisputeOutcome.REFUND
                refundAmount != null && refundAmount > 0.0 -> DisputeOutcome.PARTIAL_REFUND
                orderStatus == PesananStatus.COMPLETED -> DisputeOutcome.RELEASE
                else -> DisputeOutcome.RELEASE
            }
        }
    }
}
