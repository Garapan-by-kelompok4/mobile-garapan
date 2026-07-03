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
import org.junit.Test

class EnsureSessionUseCaseTest {

    @Test
    fun `returns Ready when a user is already in the session`() = runBlocking {
        val sessionRepository = FakeSessionRepository(initialUser = sampleUser())
        val useCase = buildUseCase(sessionRepository, FakeAuthRepository(accessToken = "token"))

        assertEquals(EnsureSessionResult.Ready, useCase())
    }

    @Test
    fun `returns RequiresLogin when no token is stored`() = runBlocking {
        val useCase = buildUseCase(FakeSessionRepository(), FakeAuthRepository(accessToken = null))

        assertEquals(EnsureSessionResult.RequiresLogin, useCase())
    }

    @Test
    fun `returns ReadyCached and seeds the session from the local snapshot`() = runBlocking {
        val cached = sampleUser()
        val sessionRepository = FakeSessionRepository(cachedUser = cached)
        val useCase = buildUseCase(sessionRepository, FakeAuthRepository(accessToken = "token"))

        assertEquals(EnsureSessionResult.ReadyCached, useCase())
        assertEquals(cached, sessionRepository.peekCurrentUser())
    }

    @Test
    fun `returns Ready and stores the user when getMe succeeds`() = runBlocking {
        val user = sampleUser()
        val sessionRepository = FakeSessionRepository()
        val authRepository = FakeAuthRepository(
            accessToken = "token",
            getMe = { Resource.Success(user) }
        )
        val useCase = buildUseCase(sessionRepository, authRepository)

        assertEquals(EnsureSessionResult.Ready, useCase())
        assertEquals(user, sessionRepository.peekCurrentUser())
    }

    @Test
    fun `returns Unavailable when getMe fails but the token survives`() = runBlocking {
        val authRepository = FakeAuthRepository(
            accessToken = "token",
            getMe = { Resource.Error("Tidak ada koneksi internet") }
        )
        val useCase = buildUseCase(FakeSessionRepository(), authRepository)

        assertEquals(EnsureSessionResult.Unavailable, useCase())
    }

    @Test
    fun `returns RequiresLogin when getMe fails and the token was cleared`() = runBlocking {
        // Mirrors the authenticator wiping tokens after a definitive refresh failure.
        val authRepository = FakeAuthRepository(accessToken = "token")
        authRepository.getMe = {
            authRepository.accessToken = null
            Resource.Error("Unauthorized")
        }
        val useCase = buildUseCase(FakeSessionRepository(), authRepository)

        assertEquals(EnsureSessionResult.RequiresLogin, useCase())
    }

    private fun buildUseCase(
        sessionRepository: SessionRepository,
        authRepository: FakeAuthRepository
    ) = EnsureSessionUseCase(
        sessionRepository = sessionRepository,
        checkAuthTokenUseCase = CheckAuthTokenUseCase(authRepository),
        loadSessionUseCase = LoadSessionUseCase(authRepository, sessionRepository)
    )

    private fun sampleUser() = User(
        id = "user-1",
        email = "budi.klien@garapan.test",
        role = Role.KLIEN,
        emailVerified = true,
        deviceToken = null,
        twoFactorEnabled = false,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
        mahasiswa = null,
        klien = null
    )

    private class FakeAuthRepository(
        var accessToken: String?,
        var getMe: suspend () -> Resource<User> = { Resource.Error("unused") }
    ) : AuthRepository by UnusedAuthRepository() {
        override suspend fun getAccessToken(): String? = accessToken
        override suspend fun getMe(): Resource<User> = getMe.invoke()
    }

    private class FakeSessionRepository(
        initialUser: User? = null,
        private val cachedUser: User? = null
    ) : SessionRepository {
        private val _currentUser = MutableStateFlow(initialUser)
        override val currentUser: StateFlow<User?> = _currentUser

        override fun peekCurrentUser(): User? = _currentUser.value

        override fun setUser(user: User) {
            _currentUser.value = user
        }

        override suspend fun restoreCachedUser(): User? {
            _currentUser.value?.let { return it }
            if (cachedUser != null) {
                _currentUser.value = cachedUser
            }
            return _currentUser.value
        }

        override fun clear() {
            _currentUser.value = null
        }
    }
}
