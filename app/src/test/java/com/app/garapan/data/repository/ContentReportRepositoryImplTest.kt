package com.app.garapan.data.repository

import com.app.garapan.data.remote.api.ContentReportApi
import com.app.garapan.data.remote.dto.CreateContentReportDto
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.ContentReportContentType
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class ContentReportRepositoryImplTest {

    @Test
    fun `submit report sends mapped dto for jasa`() = runBlocking {
        var capturedRequest: CreateContentReportDto? = null
        val repository = ContentReportRepositoryImpl(
            contentReportApi = object : ContentReportApi {
                override suspend fun createContentReport(body: CreateContentReportDto) =
                    JsonObject().also { capturedRequest = body }
            }
        )

        val result = repository.submitReport(
            contentType = ContentReportContentType.JASA,
            contentId = "jasa-123",
            reason = "Deskripsi jasa tidak sesuai dengan layanan yang ditawarkan."
        )

        assertEquals(
            CreateContentReportDto(
                contentType = "JASA",
                contentId = "jasa-123",
                reason = "Deskripsi jasa tidak sesuai dengan layanan yang ditawarkan."
            ),
            capturedRequest
        )
        assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun `submit report sends mapped dto for project`() = runBlocking {
        var capturedRequest: CreateContentReportDto? = null
        val repository = ContentReportRepositoryImpl(
            contentReportApi = object : ContentReportApi {
                override suspend fun createContentReport(body: CreateContentReportDto) =
                    JsonObject().also { capturedRequest = body }
            }
        )

        val result = repository.submitReport(
            contentType = ContentReportContentType.PROJECT,
            contentId = "project-456",
            reason = "Proyek ini terlihat seperti penipuan dan melanggar aturan."
        )

        assertEquals(
            CreateContentReportDto(
                contentType = "PROJECT",
                contentId = "project-456",
                reason = "Proyek ini terlihat seperti penipuan dan melanggar aturan."
            ),
            capturedRequest
        )
        assertEquals(Resource.Success(Unit), result)
    }

    @Test
    fun `submit report maps api error`() = runBlocking {
        val repository = ContentReportRepositoryImpl(
            contentReportApi = object : ContentReportApi {
                override suspend fun createContentReport(body: CreateContentReportDto): com.google.gson.JsonElement {
                    throw HttpException(
                        Response.error<Any>(
                            409,
                            """{"message":"You already have a pending report for this content"}"""
                                .toResponseBody("application/json".toMediaType())
                        )
                    )
                }
            }
        )

        val result = repository.submitReport(
            contentType = ContentReportContentType.JASA,
            contentId = "jasa-123",
            reason = "Konten ini terlihat menyesatkan dan tidak sesuai aturan."
        )

        assertTrue(result is Resource.Error)
        assertEquals(
            "You already have a pending report for this content",
            (result as Resource.Error).message
        )
    }
}
