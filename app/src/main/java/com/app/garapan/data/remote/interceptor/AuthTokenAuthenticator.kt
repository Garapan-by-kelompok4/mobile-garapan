package com.app.garapan.data.remote.interceptor

import com.app.garapan.BuildConfig
import com.app.garapan.data.local.AuthTokenStore
import com.app.garapan.data.remote.dto.AuthTokensDto
import com.app.garapan.domain.model.AuthTokens
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Route
import javax.inject.Inject

class AuthTokenAuthenticator @Inject constructor(
    private val tokenStore: AuthTokenStore,
    private val gson: Gson
) : Authenticator {
    private val refreshClient by lazy { OkHttpClient.Builder().build() }

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        if (response.request.url.encodedPath.isPublicAuthPath() || response.responseCount() >= 2) {
            return null
        }

        val refreshToken = runBlocking { tokenStore.getRefreshToken() } ?: return null
        val refreshed = refreshTokens(refreshToken) ?: run {
            runBlocking { tokenStore.clearTokens() }
            return null
        }

        runBlocking { tokenStore.saveTokens(refreshed) }
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.accessToken}")
            .build()
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
}
