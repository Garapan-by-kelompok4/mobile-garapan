package com.app.garapan.presentation.screen.forgot_password

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository
import com.app.garapan.domain.usecase.ForgotPasswordUseCase
import com.app.garapan.presentation.navigation.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

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
    fun `successful request shows token entry action without navigating`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEmailChanged(" Student@Example.com ")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.canEnterResetToken)
        assertEquals(
            "If that email is registered, a reset token has been sent.",
            viewModel.uiState.value.infoMessage
        )
    }

    @Test
    fun `enter reset token navigates with trimmed email`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEmailChanged(" Student@Example.com ")
        viewModel.onSubmit()
        advanceUntilIdle()
        val events = mutableListOf<ForgotPasswordEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.onEnterResetToken()
        advanceUntilIdle()

        assertEquals(
            ForgotPasswordEvent.Navigate(Routes.resetPasswordRoute("Student@Example.com")),
            events.single()
        )
    }

    private fun createViewModel(
        forgotPasswordResult: Resource<Boolean> = Resource.Success(true)
    ): ForgotPasswordViewModel {
        val repository = object : AuthRepository {
            override suspend fun forgotPassword(email: String): Resource<Boolean> {
                assertEquals("Student@Example.com", email)
                return forgotPasswordResult
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
            override suspend fun resetPassword(token: String, newPassword: String): Resource<Boolean> = unused()
            override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Boolean> = unused()
            override suspend fun refresh(): Resource<AuthTokens> = unused()
            override suspend fun logout(): Resource<Boolean> = unused()
            override suspend fun getMe(): Resource<User> = unused()
            override suspend fun updateProfile(params: UpdateProfileParams): Resource<User> = unused()
            override suspend fun uploadAvatar(image: PortofolioImage): Resource<User> = unused()

            private fun unused(): Nothing = error("unused")
        }
        return ForgotPasswordViewModel(ForgotPasswordUseCase(repository))
    }
}
