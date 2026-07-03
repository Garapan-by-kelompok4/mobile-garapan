package com.app.garapan.domain.repository

import com.app.garapan.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val currentUser: StateFlow<User?>

    fun peekCurrentUser(): User?

    fun setUser(user: User)

    /**
     * Restores the last known user from local storage into the session, if the
     * session is empty. Returns the restored (or already present) user, or null
     * when nothing usable is stored. Callers that get a cached user back should
     * still refresh from the network in the background.
     */
    suspend fun restoreCachedUser(): User?

    fun clear()
}
