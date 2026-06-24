package com.app.garapan.domain.repository

import com.app.garapan.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val currentUser: StateFlow<User?>

    fun setUser(user: User)

    fun clear()
}
