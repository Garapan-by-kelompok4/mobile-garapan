package com.app.garapan.domain.model

/**
 * Account status as defined by the backend `ProfileStatus` enum.
 * [apiValue] is the value sent to / received from the API; [label] is the Indonesian UI label.
 */
enum class ProfileStatus(val apiValue: String, val label: String) {
    INDIVIDUAL("INDIVIDUAL", "Individu"),
    COMPANY("COMPANY", "Perusahaan"),
    STARTUP("STARTUP", "Startup"),
    GOVERNMENT("GOVERNMENT", "Pemerintah");

    companion object {
        fun fromApiValue(value: String?): ProfileStatus? =
            value?.let { raw -> entries.firstOrNull { it.apiValue == raw } }
    }
}
