package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ContentReportContentType
import com.app.garapan.domain.repository.ContentReportRepository
import javax.inject.Inject

class SubmitContentReportUseCase @Inject constructor(
    private val repository: ContentReportRepository
) {
    suspend operator fun invoke(
        contentType: ContentReportContentType,
        contentId: String,
        reason: String
    ): Resource<Unit> = repository.submitReport(contentType, contentId, reason)
}
