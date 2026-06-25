package com.app.garapan.presentation.util

import com.app.garapan.domain.model.PesananStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object PesananDisplayMapper {
    fun statusLabel(status: PesananStatus): String = when (status) {
        PesananStatus.PENDING -> "MENUNGGU BAYAR"
        PesananStatus.PAID -> "DIBAYAR"
        PesananStatus.IN_PROGRESS -> "DIPROSES"
        PesananStatus.DELIVERED -> "DIKIRIM"
        PesananStatus.COMPLETED -> "SELESAI"
        PesananStatus.DISPUTED -> "DISPUTE"
    }

    fun formatOrderDate(isoDate: String): String {
        if (isoDate.isBlank()) return ""
        return runCatching {
            val instant = Instant.parse(isoDate)
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            val today = LocalDate.now()
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))
            val time = instant.atZone(ZoneId.systemDefault()).format(timeFormatter)
            when {
                date == today -> "Hari ini, $time"
                date == today.minusDays(1) -> "Kemarin, $time"
                else -> {
                    val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
                    "${date.format(dateFormatter)}, $time"
                }
            }
        }.getOrDefault(isoDate)
    }

    fun orderTitle(jasaTitle: String, projectId: String?): String {
        val title = jasaTitle.ifBlank { "Pesanan" }
        return if (projectId.isNullOrBlank()) {
            "Pembayaran Jasa: $title"
        } else {
            "Pembayaran Proyek: $title"
        }
    }
}
