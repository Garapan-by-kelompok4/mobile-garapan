package com.app.garapan.domain.model

enum class ContentReportContentType(val apiValue: String) {
    JASA("JASA"),
    PROJECT("PROJECT");

    companion object {
        fun fromRouteValue(value: String): ContentReportContentType? =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }
    }
}
