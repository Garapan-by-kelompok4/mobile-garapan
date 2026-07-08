package com.app.garapan.domain.model

data class TakenProject(
    val project: Project,
    val orderId: String? = null,
    val orderStatus: PesananStatus? = null
)
