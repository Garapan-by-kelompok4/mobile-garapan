package com.app.garapan.data.remote.interceptor

import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInterceptorTest {

    @Test
    fun `password reset endpoints are public auth paths`() {
        assertTrue("/auth/forgot-password".isPublicAuthPath())
        assertTrue("/auth/reset-password".isPublicAuthPath())
    }
}
