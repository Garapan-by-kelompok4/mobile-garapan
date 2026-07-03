package com.app.garapan.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.SessionRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson,
    private val appScope: CoroutineScope
) : SessionRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override fun peekCurrentUser(): User? = _currentUser.value

    override fun setUser(user: User) {
        _currentUser.value = user
        appScope.launch {
            runCatching {
                dataStore.edit { it[CACHED_USER_KEY] = gson.toJson(user) }
            }
        }
    }

    override suspend fun restoreCachedUser(): User? {
        _currentUser.value?.let { return it }

        val json = runCatching { dataStore.data.first()[CACHED_USER_KEY] }.getOrNull()
            ?: return null
        val user = parseUser(json)
        if (user == null) {
            appScope.launch {
                runCatching { dataStore.edit { it.remove(CACHED_USER_KEY) } }
            }
            return null
        }
        // Only seed an empty session; a fresh user loaded meanwhile wins.
        if (_currentUser.value == null) {
            _currentUser.value = user
        }
        return _currentUser.value
    }

    override fun clear() {
        _currentUser.value = null
        appScope.launch {
            runCatching { dataStore.edit { it.remove(CACHED_USER_KEY) } }
        }
    }

    private fun parseUser(json: String): User? = runCatching {
        gson.fromJson(json, User::class.java)
            // Gson bypasses constructors, so a snapshot from an older app version
            // can leave non-null fields null; touching them here surfaces that as
            // a parse failure instead of a crash later.
            ?.also { it.role.name }
            ?.takeIf { it.id.isNotBlank() }
    }.getOrNull()

    private companion object {
        val CACHED_USER_KEY = stringPreferencesKey("cached_user")
    }
}
