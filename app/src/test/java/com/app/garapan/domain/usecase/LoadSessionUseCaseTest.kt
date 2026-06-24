package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository
import com.app.garapan.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoadSessionUseCaseTest {

    @Test
    fun `stores user in session when getMe succeeds`() = runBlocking {
        val user = sampleUser(Role.KLIEN)
        val authRepository = FakeAuthRepository(getMeResult = Resource.Success(user))
        val sessionRepository = FakeSessionRepository()
        val useCase = LoadSessionUseCase(authRepository, sessionRepository)

        val result = useCase()

        assertEquals(Resource.Success(user), result)
        assertEquals(user, sessionRepository.currentUser.value)
    }

    @Test
    fun `does not store user when getMe fails`() = runBlocking {
        val authRepository = FakeAuthRepository(getMeResult = Resource.Error("Unauthorized"))
        val sessionRepository = FakeSessionRepository()
        val useCase = LoadSessionUseCase(authRepository, sessionRepository)

        val result = useCase()

        assertEquals(Resource.Error("Unauthorized"), result)
        assertNull(sessionRepository.currentUser.value)
    }

    private fun sampleUser(role: Role) = User(
        id = "user-1",
        email = "budi.klien@garapan.test",
        role = role,
        emailVerified = true,
        deviceToken = null,
        twoFactorEnabled = false,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
        mahasiswa = null,
        klien = null
    )

    private class FakeAuthRepository(
        private val getMeResult: Resource<User>
    ) : AuthRepository by UnusedAuthRepository() {
        override suspend fun getMe(): Resource<User> = getMeResult
    }

    private class FakeSessionRepository : SessionRepository {
        private val _currentUser = MutableStateFlow<User?>(null)
        override val currentUser: StateFlow<User?> = _currentUser

        override fun setUser(user: User) {
            _currentUser.value = user
        }

        override fun clear() {
            _currentUser.value = null
        }
    }
}
