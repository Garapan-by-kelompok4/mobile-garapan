package com.app.garapan.data.remote.interceptor

import com.app.garapan.BuildConfig
import com.app.garapan.data.local.AuthTokenStore
import com.app.garapan.domain.repository.SessionRepository
import com.app.garapan.data.remote.dto.AuthTokensDto
import com.app.garapan.domain.model.AuthTokens
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Route
import javax.inject.Inject

class AuthTokenAuthenticator @Inject constructor(
    private val tokenStore: AuthTokenStore,
    private val sessionRepository: SessionRepository,
    private val gson: Gson
) : Authenticator {
    private val refreshClient by lazy { OkHttpClient.Builder().build() }

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        if (response.request.url.encodedPath.isPublicAuthPath() || response.responseCount() >= 2) {
            return null
        }

        return runBlocking {
            refreshMutex.withLock {
                authenticateWithLockedRefresh(response)
            }
        }
    }

    private suspend fun authenticateWithLockedRefresh(response: okhttp3.Response): Request? {
        val failedAccessToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.takeIf { it.isNotBlank() }
        val currentAccessToken = if (tokenStore.isCacheLoaded()) {
            tokenStore.getCachedAccessToken()
        } else {
            tokenStore.getAccessToken()
        }

        if (!failedAccessToken.isNullOrBlank() &&
            !currentAccessToken.isNullOrBlank() &&
            failedAccessToken != currentAccessToken
        ) {
            return response.request.withBearerToken(currentAccessToken)
        }

        val refreshToken = if (tokenStore.isCacheLoaded()) {
            tokenStore.getCachedRefreshToken()
        } else {
            tokenStore.getRefreshToken()
        } ?: return null
        val refreshed = refreshTokens(refreshToken)

        if (refreshed == null) {
            val currentRefreshToken = if (tokenStore.isCacheLoaded()) {
                tokenStore.getCachedRefreshToken()
            } else {
                tokenStore.getRefreshToken()
            }
            if (currentRefreshToken == refreshToken) {
                tokenStore.clearTokens()
                sessionRepository.clear()
            }
            return null
        }

        tokenStore.saveTokens(refreshed)
        return response.request.withBearerToken(refreshed.accessToken)
    }

    private fun refreshTokens(refreshToken: String): AuthTokens? =
        runCatching {
            val body = gson.toJson(mapOf("refreshToken" to refreshToken))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}auth/refresh")
                .post(body)
                .build()
            refreshClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val responseBody = response.body?.string() ?: return null
                val dto = gson.fromJson(responseBody, AuthTokensDto::class.java)
                AuthTokens(dto.accessToken, dto.refreshToken)
            }
        }.getOrNull()

    private fun okhttp3.Response.responseCount(): Int {
        var result = 1
        var prior = priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    private fun Request.withBearerToken(accessToken: String): Request =
        newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

    companion object {
        private val refreshMutex = Mutex()
    }
}
