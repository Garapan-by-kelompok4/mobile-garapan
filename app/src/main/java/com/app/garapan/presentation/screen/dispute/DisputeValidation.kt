package com.app.garapan.presentation.screen.dispute

object DisputeValidation {
    const val MIN_REASON_LENGTH = 10
    const val MAX_REASON_LENGTH = 1000

    fun validateReason(reason: String): String? {
        val trimmed = reason.trim()
        if (trimmed.isBlank()) return "Alasan dispute tidak boleh kosong."
        if (trimmed.length < MIN_REASON_LENGTH) {
            return "Alasan dispute minimal $MIN_REASON_LENGTH karakter."
        }
        if (trimmed.length > MAX_REASON_LENGTH) {
            return "Alasan dispute maksimal $MAX_REASON_LENGTH karakter."
        }
        return null
    }
}
