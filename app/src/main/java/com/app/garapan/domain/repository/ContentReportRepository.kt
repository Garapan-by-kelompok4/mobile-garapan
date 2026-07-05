package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ContentReportContentType

interface ContentReportRepository {
    suspend fun submitReport(
        contentType: ContentReportContentType,
        contentId: String,
        reason: String
    ): Resource<Unit>
}
