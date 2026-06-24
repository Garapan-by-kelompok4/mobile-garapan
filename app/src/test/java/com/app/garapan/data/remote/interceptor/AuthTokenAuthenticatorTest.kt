package com.app.garapan.data.remote.interceptor

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.app.garapan.data.local.AuthTokenStore
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.repository.SessionRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class AuthTokenAuthenticatorTest {

    @Test
    fun `does not clear tokens when another request already refreshed`() = runBlocking {
        val tokenStore = newTokenStore()
        tokenStore.saveTokens(AuthTokens(accessToken = "fresh-access", refreshToken = "fresh-refresh"))
        val authenticator = AuthTokenAuthenticator(tokenStore, NoOpSessionRepository(), Gson())
        val failedRequest = Request.Builder()
            .url("https://api-garapan.up.railway.app/api/users/me")
            .header("Authorization", "Bearer stale-access")
            .build()

        val retriedRequest = authenticator.authenticate(null, unauthorizedResponse(failedRequest))

        assertEquals("Bearer fresh-access", retriedRequest?.header("Authorization"))
        assertEquals("fresh-access", tokenStore.getAccessToken())
        assertEquals("fresh-refresh", tokenStore.getRefreshToken())
    }

    private fun unauthorizedResponse(request: Request): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody())
            .build()

    private fun newTokenStore(): AuthTokenStore {
        val file = Files.createTempDirectory("garapan-authenticator-test")
            .resolve("preferences.preferences_pb")
            .toFile()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { file }
        )
        return AuthTokenStore(dataStore)
    }

    private class NoOpSessionRepository : SessionRepository {
        override val currentUser: StateFlow<com.app.garapan.domain.model.User?> = MutableStateFlow(null)

        override fun peekCurrentUser(): com.app.garapan.domain.model.User? = null

        override fun setUser(user: com.app.garapan.domain.model.User) = Unit

        override fun clear() = Unit
    }
}
