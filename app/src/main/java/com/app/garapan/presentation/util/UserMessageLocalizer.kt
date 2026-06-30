package com.app.garapan.presentation.util

object UserMessageLocalizer {
    fun localize(apiMessage: String): String {
        val normalized = apiMessage.trim()
        return when {
            normalized.equals("Project not found", ignoreCase = true) ->
                "Proyek tidak ditemukan."
            normalized.contains("cannot be deleted because orders reference", ignoreCase = true) ->
                "Layanan tidak dapat dihapus karena sudah ada pesanan yang terhubung."
            normalized.equals("Current password is incorrect", ignoreCase = true) ->
                "Kata sandi saat ini salah."
            normalized.equals("You cannot order your own jasa", ignoreCase = true) ->
                "Anda tidak dapat memesan jasa milik sendiri."
            normalized.equals("You do not own this order", ignoreCase = true) ->
                "Anda tidak memiliki akses ke pesanan ini."
            normalized.equals("Request failed. Please try again.", ignoreCase = true) ->
                "Permintaan gagal. Silakan coba lagi."
            normalized.equals("Something went wrong. Please try again.", ignoreCase = true) ->
                "Terjadi kesalahan. Silakan coba lagi."
            else -> normalized
        }
    }
}
