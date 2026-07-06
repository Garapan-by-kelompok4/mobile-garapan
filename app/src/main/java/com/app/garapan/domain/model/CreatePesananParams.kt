package com.app.garapan.domain.model

data class CreatePesananParams(
    val jasaId: String,
    val idempotencyKey: String? = null
)
