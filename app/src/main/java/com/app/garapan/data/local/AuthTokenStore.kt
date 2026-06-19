package com.app.garapan.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.garapan.domain.model.AuthTokens
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun getAccessToken(): String? =
        dataStore.data.first()[ACCESS_TOKEN_KEY]

    suspend fun getRefreshToken(): String? =
        dataStore.data.first()[REFRESH_TOKEN_KEY]

    suspend fun saveTokens(tokens: AuthTokens) {
        dataStore.edit {
            it[ACCESS_TOKEN_KEY] = tokens.accessToken
            it[REFRESH_TOKEN_KEY] = tokens.refreshToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
            it.remove(LEGACY_AUTH_TOKEN_KEY)
        }
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val LEGACY_AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
