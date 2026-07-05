package com.app.garapan.presentation.screen.report_content

object ReportValidation {
    const val MIN_REASON_LENGTH = 10
    const val MAX_REASON_LENGTH = 1000

    fun validateReason(reason: String): String? {
        val trimmed = reason.trim()
        if (trimmed.isBlank()) return "Alasan laporan tidak boleh kosong."
        if (trimmed.length < MIN_REASON_LENGTH) {
            return "Alasan laporan minimal $MIN_REASON_LENGTH karakter."
        }
        if (trimmed.length > MAX_REASON_LENGTH) {
            return "Alasan laporan maksimal $MAX_REASON_LENGTH karakter."
        }
        return null
    }
}
