package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OtpUseCaseTest {

    @Test
    fun `verify otp forwards pre auth token and otp to repository`() = runBlocking {
        val repository = FakeAuthRepository()
        val useCase = VerifyOtpUseCase(repository)

        val result = useCase("pre-auth-token", "123456")

        assertEquals("pre-auth-token", repository.verifyPreAuthToken)
        assertEquals("123456", repository.verifyOtp)
        assertEquals(Resource.Success(AuthTokens("access", "refresh")), result)
    }

    @Test
    fun `resend otp forwards pre auth token to repository`() = runBlocking {
        val repository = FakeAuthRepository()
        val useCase = ResendOtpUseCase(repository)

        val result = useCase("pre-auth-token")

        assertEquals("pre-auth-token", repository.resendPreAuthToken)
        assertEquals(Resource.Success(true), result)
    }

    private class FakeAuthRepository : AuthRepository {
        var verifyPreAuthToken: String? = null
        var verifyOtp: String? = null
        var resendPreAuthToken: String? = null

        override suspend fun getAccessToken(): String? = null
        override suspend fun getRefreshToken(): String? = null
        override suspend fun saveAuthTokens(tokens: AuthTokens) = Unit
        override suspend fun clearAuthTokens() = Unit
        override suspend fun register(email: String, password: String, role: Role): Resource<User> = unused()
        override suspend fun login(email: String, password: String): Resource<LoginResult> = unused()
        override suspend fun googleSignIn(idToken: String, role: Role?): Resource<AuthTokens> = unused()
        override suspend fun verifyEmail(token: String): Resource<Boolean> = unused()
        override suspend fun resendVerification(email: String): Resource<Boolean> = unused()
        override suspend fun forgotPassword(email: String): Resource<Boolean> = unused()
        override suspend fun resetPassword(token: String, newPassword: String): Resource<Boolean> = unused()
        override suspend fun refresh(): Resource<AuthTokens> = unused()
        override suspend fun logout(): Resource<Boolean> = unused()
        override suspend fun getMe(): Resource<User> = unused()
        override suspend fun updateProfile(params: UpdateProfileParams): Resource<User> = unused()
        override suspend fun uploadAvatar(image: PortofolioImage): Resource<User> = unused()

        override suspend fun verifyTwoFactor(preAuthToken: String, otp: String): Resource<AuthTokens> {
            verifyPreAuthToken = preAuthToken
            verifyOtp = otp
            return Resource.Success(AuthTokens("access", "refresh"))
        }

        override suspend fun resendOtp(preAuthToken: String): Resource<Boolean> {
            resendPreAuthToken = preAuthToken
            return Resource.Success(true)
        }

        private fun unused(): Nothing = error("Unused")
    }
}
