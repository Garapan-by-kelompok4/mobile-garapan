package com.app.garapan.domain.model

enum class JasaStatus {
    ACTIVE,
    INACTIVE;

    companion object {
        fun fromApiValue(value: String): JasaStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}
