package com.app.garapan.data.remote.error

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiErrorMapperTest {

    @Test
    fun `maps string message from backend error envelope`() {
        val exception = httpException(
            """{"statusCode":401,"message":"Email address is not verified","error":"Unauthorized","timestamp":"2026-06-19T00:00:00.000Z","path":"/api/auth/login"}"""
        )

        assertEquals("Email address is not verified", ApiErrorMapper.toMessage(exception))
    }

    @Test
    fun `maps array message from backend error envelope into one message`() {
        val exception = httpException(
            """{"statusCode":400,"message":["email must be an email","password is weak"],"error":"Bad Request","timestamp":"2026-06-19T00:00:00.000Z","path":"/api/auth/register"}"""
        )

        assertEquals("email must be an email\npassword is weak", ApiErrorMapper.toMessage(exception))
    }

    @Test
    fun `maps too many requests before backend error envelope`() {
        val exception = httpException(
            """{"statusCode":429,"message":"Too Many Requests","error":"Too Many Requests"}""",
            statusCode = 429
        )

        assertEquals(
            "Too many attempts. Please wait a minute and try again.",
            ApiErrorMapper.toMessage(exception)
        )
    }

    @Test
    fun `maps network failure to user facing message`() {
        assertEquals(
            "Network error while uploading. Check your connection and try again.",
            ApiErrorMapper.toMessage(IOException("timeout"))
        )
    }

    private fun httpException(json: String, statusCode: Int = 400): HttpException {
        val body = json.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(statusCode, body))
    }
}
