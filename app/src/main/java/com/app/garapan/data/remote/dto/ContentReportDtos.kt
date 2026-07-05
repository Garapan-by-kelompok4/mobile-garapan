package com.app.garapan.data.remote.dto

data class CreateContentReportDto(
    val contentType: String,
    val contentId: String,
    val reason: String
)
