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
    fun `maps network failure to user facing message`() {
        assertEquals(
            "Unable to connect. Check your internet connection and try again.",
            ApiErrorMapper.toMessage(IOException("timeout"))
        )
    }

    private fun httpException(json: String): HttpException {
        val body = json.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(400, body))
    }
}
