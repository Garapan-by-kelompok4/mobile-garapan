package com.app.garapan.presentation.screen.auth

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.MahasiswaProfile
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.Skill
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository
import com.app.garapan.domain.repository.SessionRepository
import com.app.garapan.domain.repository.SkillRepository
import com.app.garapan.domain.usecase.GetSkillListUseCase
import com.app.garapan.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupAccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `student setup sends full name as display name`() = runTest {
        val authRepository = CapturingAuthRepository()
        val viewModel = createViewModel(authRepository)

        viewModel.onStudentFullNameChanged("Haykal Rafi")
        viewModel.onUniversityChanged("Binus University")
        viewModel.onToggleStudentExpertise("Mobile Development")
        viewModel.onComplete("student")
        advanceUntilIdle()

        assertEquals("Haykal Rafi", authRepository.capturedParams?.displayName)
    }

    private fun createViewModel(
        authRepository: CapturingAuthRepository
    ): SetupAccountViewModel {
        val sessionRepository = FakeSessionRepository()
        return SetupAccountViewModel(
            getSkillListUseCase = GetSkillListUseCase(FakeSkillRepository()),
            updateProfileUseCase = UpdateProfileUseCase(authRepository, sessionRepository)
        )
    }

    private class FakeSkillRepository : SkillRepository {
        override suspend fun getSkillList(): Resource<List<Skill>> =
            Resource.Success(
                listOf(
                    Skill(
                        id = "skill-1",
                        name = "Mobile Development",
                        kategoriId = "cat-1",
                        kategoriName = "Programming"
                    )
                )
            )
    }

    private class CapturingAuthRepository : AuthRepository {
        var capturedParams: UpdateProfileParams? = null

        override suspend fun updateProfile(params: UpdateProfileParams): Resource<User> {
            capturedParams = params
            return Resource.Success(sampleUser(params.displayName))
        }

        override suspend fun getAccessToken(): String? = unused()
        override suspend fun getRefreshToken(): String? = unused()
        override suspend fun saveAuthTokens(tokens: AuthTokens) = unused()
        override suspend fun clearAuthTokens() = unused()
        override suspend fun register(email: String, password: String, role: Role): Resource<User> = unused()
        override suspend fun login(email: String, password: String): Resource<LoginResult> = unused()
        override suspend fun googleSignIn(idToken: String, role: Role?): Resource<AuthTokens> = unused()
        override suspend fun verifyTwoFactor(preAuthToken: String, otp: String): Resource<AuthTokens> = unused()
        override suspend fun resendOtp(preAuthToken: String): Resource<Boolean> = unused()
        override suspend fun verifyEmail(token: String): Resource<Boolean> = unused()
        override suspend fun resendVerification(email: String): Resource<Boolean> = unused()
        override suspend fun forgotPassword(email: String): Resource<Boolean> = unused()
        override suspend fun resetPassword(token: String, newPassword: String): Resource<Boolean> = unused()
        override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Boolean> = unused()
        override suspend fun refresh(): Resource<AuthTokens> = unused()
        override suspend fun logout(): Resource<Boolean> = unused()
        override suspend fun getMe(): Resource<User> = unused()
        override suspend fun uploadAvatar(image: PortofolioImage): Resource<User> = unused()

        private fun unused(): Nothing = error("Unused")
    }

    private class FakeSessionRepository : SessionRepository {
        private val user = MutableStateFlow<User?>(null)
        override val currentUser: StateFlow<User?> = user

        override fun peekCurrentUser(): User? = user.value

        override fun setUser(user: User) {
            this.user.value = user
        }

        override suspend fun restoreCachedUser(): User? = user.value

        override fun clear() {
            user.value = null
        }
    }

    private companion object {
        fun sampleUser(displayName: String?) = User(
            id = "user-1",
            email = "haykal@example.com",
            role = Role.MAHASISWA,
            emailVerified = true,
            deviceToken = null,
            twoFactorEnabled = false,
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-01-01T00:00:00Z",
            mahasiswa = MahasiswaProfile(
                id = "mahasiswa-1",
                userId = "user-1",
                university = "Binus University",
                skills = emptyList(),
                bio = "",
                walletBalance = "0",
                rating = 0.0
            ),
            klien = null,
            displayName = displayName
        )
    }
}
