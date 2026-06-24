package com.app.garapan.data.repository

import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor() : SessionRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override fun setUser(user: User) {
        _currentUser.value = user
    }

    override fun clear() {
        _currentUser.value = null
    }
}
