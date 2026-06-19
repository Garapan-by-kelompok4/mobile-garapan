package com.app.garapan.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.garapan.domain.model.AuthTokens
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class TokenSnapshot(
    val accessToken: String?,
    val refreshToken: String?
)

@Singleton
class AuthTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    @Volatile
    private var cachedTokens: TokenSnapshot? = null

    @Volatile
    private var cacheLoaded: Boolean = false

    fun getCachedAccessToken(): String? =
        cachedTokens?.accessToken

    fun getCachedRefreshToken(): String? =
        cachedTokens?.refreshToken

    fun isCacheLoaded(): Boolean = cacheLoaded

    suspend fun getAccessToken(): String? =
        getTokenSnapshot().accessToken

    suspend fun getRefreshToken(): String? =
        getTokenSnapshot().refreshToken

    suspend fun saveTokens(tokens: AuthTokens) {
        dataStore.edit {
            it[ACCESS_TOKEN_KEY] = tokens.accessToken
            it[REFRESH_TOKEN_KEY] = tokens.refreshToken
        }
        cachedTokens = TokenSnapshot(tokens.accessToken, tokens.refreshToken)
        cacheLoaded = true
    }

    suspend fun clearTokens() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
            it.remove(LEGACY_AUTH_TOKEN_KEY)
        }
        cachedTokens = TokenSnapshot(accessToken = null, refreshToken = null)
        cacheLoaded = true
    }

    private suspend fun getTokenSnapshot(): TokenSnapshot {
        if (cacheLoaded) {
            return cachedTokens ?: TokenSnapshot(accessToken = null, refreshToken = null)
        }

        return dataStore.data.first().let {
            TokenSnapshot(
                accessToken = it[ACCESS_TOKEN_KEY],
                refreshToken = it[REFRESH_TOKEN_KEY]
            )
        }.also {
            cachedTokens = it
            cacheLoaded = true
        }
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val LEGACY_AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
