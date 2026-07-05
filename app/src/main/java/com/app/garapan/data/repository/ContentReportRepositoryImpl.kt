package com.app.garapan.data.repository

import com.app.garapan.data.remote.api.ContentReportApi
import com.app.garapan.data.remote.dto.CreateContentReportDto
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ContentReportContentType
import com.app.garapan.domain.repository.ContentReportRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ContentReportRepositoryImpl @Inject constructor(
    private val contentReportApi: ContentReportApi
) : ContentReportRepository {

    override suspend fun submitReport(
        contentType: ContentReportContentType,
        contentId: String,
        reason: String
    ): Resource<Unit> = safeApiCall {
        contentReportApi.createContentReport(
            CreateContentReportDto(
                contentType = contentType.apiValue,
                contentId = contentId,
                reason = reason
            )
        )
        Unit
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
