package com.app.garapan.domain.model

enum class PaymentMethod {
    GOPAY,
    OVO,
    QRIS;

    fun toApiValue(): String = name

    companion object {
        fun fromApiValue(value: String): PaymentMethod =
            entries.firstOrNull { it.name == value.uppercase() } ?: GOPAY
    }
}
