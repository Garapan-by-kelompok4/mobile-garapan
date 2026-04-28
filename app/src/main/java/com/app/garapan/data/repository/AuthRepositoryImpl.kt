package com.app.garapan.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.garapan.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    companion object {
        val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    override suspend fun getAuthToken(): String? =
        dataStore.data.first()[AUTH_TOKEN_KEY]

    override suspend fun saveAuthToken(token: String) {
        dataStore.edit { it[AUTH_TOKEN_KEY] = token }
    }

    override suspend fun clearAuthToken() {
        dataStore.edit { it.remove(AUTH_TOKEN_KEY) }
    }
}
