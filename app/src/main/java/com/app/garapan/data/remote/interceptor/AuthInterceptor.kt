package com.app.garapan.data.remote.interceptor

import com.app.garapan.data.local.AuthTokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.encodedPath.isPublicAuthPath()) {
            return chain.proceed(request)
        }

        val accessToken = if (tokenStore.isCacheLoaded()) {
            tokenStore.getCachedAccessToken()
        } else {
            runBlocking { tokenStore.getAccessToken() }
        }
        val authedRequest = if (accessToken.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
        return chain.proceed(authedRequest)
    }
}

internal fun String.isPublicAuthPath(): Boolean =
    PUBLIC_AUTH_PATHS.any { endsWith(it) }

private val PUBLIC_AUTH_PATHS = setOf(
    "/auth/register",
    "/auth/login",
    "/auth/verify-email",
    "/auth/resend-verification",
    "/auth/forgot-password",
    "/auth/reset-password",
    "/auth/refresh",
    "/auth/logout",
    "/auth/2fa/verify",
    "/auth/2fa/resend"
)
