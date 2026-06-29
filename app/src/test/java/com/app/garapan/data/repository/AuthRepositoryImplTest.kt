package com.app.garapan.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.app.garapan.data.local.AuthTokenStore
import com.app.garapan.data.remote.api.AuthApi
import com.app.garapan.data.remote.api.UsersApi
import com.app.garapan.data.remote.dto.AuthTokensDto
import com.app.garapan.data.remote.dto.ForgotPasswordRequestDto
import com.app.garapan.data.remote.dto.ForgotPasswordResponseDto
import com.app.garapan.data.remote.dto.GoogleSignInRequestDto
import com.app.garapan.data.remote.dto.LoginRequestDto
import com.app.garapan.data.remote.dto.LoginResponseDto
import com.app.garapan.data.remote.dto.LogoutRequestDto
import com.app.garapan.data.remote.dto.LogoutResponseDto
import com.app.garapan.data.remote.dto.RefreshRequestDto
import com.app.garapan.data.remote.dto.RegisterRequestDto
import com.app.garapan.data.remote.dto.ResendTwoFactorRequestDto
import com.app.garapan.data.remote.dto.ResendTwoFactorResponseDto
import com.app.garapan.data.remote.dto.ResendVerificationRequestDto
import com.app.garapan.data.remote.dto.ResendVerificationResponseDto
import com.app.garapan.data.remote.dto.ResetPasswordRequestDto
import com.app.garapan.data.remote.dto.ResetPasswordResponseDto
import com.app.garapan.data.remote.dto.TwoFactorVerifyRequestDto
import com.app.garapan.data.remote.dto.UpdateProfileRequestDto
import com.app.garapan.data.remote.dto.UserDto
import com.app.garapan.data.remote.dto.VerifyEmailRequestDto
import com.app.garapan.data.remote.dto.VerifyEmailResponseDto
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.Role
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class AuthRepositoryImplTest {

    @Test
    fun `forgot password returns sent flag and passes email`() = runBlocking {
        var capturedRequest: ForgotPasswordRequestDto? = null
        val repository = AuthRepositoryImpl(
            authApi = object : AuthApi by unusedAuthApi() {
                override suspend fun forgotPassword(body: ForgotPasswordRequestDto): ForgotPasswordResponseDto {
                    capturedRequest = body
                    return ForgotPasswordResponseDto(sent = true)
                }
            },
            usersApi = unusedUsersApi(),
            tokenStore = newTokenStore()
        )

        val result = repository.forgotPassword("user@example.com")

        assertEquals(ForgotPasswordRequestDto(email = "user@example.com"), capturedRequest)
        assertEquals(Resource.Success(true), result)
    }

    @Test
    fun `reset password returns reset flag and passes token and new password`() = runBlocking {
        var capturedRequest: ResetPasswordRequestDto? = null
        val repository = AuthRepositoryImpl(
            authApi = object : AuthApi by unusedAuthApi() {
                override suspend fun resetPassword(body: ResetPasswordRequestDto): ResetPasswordResponseDto {
                    capturedRequest = body
                    return ResetPasswordResponseDto(reset = true)
                }
            },
            usersApi = unusedUsersApi(),
            tokenStore = newTokenStore()
        )

        val result = repository.resetPassword("reset-token-123456789012345678901234567890", "Strong1!")

        assertEquals(
            ResetPasswordRequestDto(
                token = "reset-token-123456789012345678901234567890",
                newPassword = "Strong1!"
            ),
            capturedRequest
        )
        assertEquals(Resource.Success(true), result)
    }

    @Test
    fun `google sign in posts id token and role then saves returned tokens`() = runBlocking {
        var capturedBody: GoogleSignInRequestDto? = null
        val tokenStore = newTokenStore()
        val repository = AuthRepositoryImpl(
            authApi = object : AuthApi by unusedAuthApi() {
                override suspend fun googleSignIn(body: GoogleSignInRequestDto): AuthTokensDto {
                    capturedBody = body
                    return AuthTokensDto(accessToken = "access-token", refreshToken = "refresh-token")
                }
            },
            usersApi = unusedUsersApi(),
            tokenStore = tokenStore
        )

        val result = repository.googleSignIn("google-id-token", Role.KLIEN)

        assertTrue(result is Resource.Success)
        val success = result as Resource.Success
        assertEquals(GoogleSignInRequestDto(idToken = "google-id-token", role = "KLIEN"), capturedBody)
        assertEquals("access-token", success.data.accessToken)
        assertEquals("refresh-token", success.data.refreshToken)
        assertEquals("access-token", tokenStore.getAccessToken())
        assertEquals("refresh-token", tokenStore.getRefreshToken())
        assertNotNull(capturedBody)
    }

    @Test
    fun `safe api call rethrows cancellation exception`() {
        val repository = AuthRepositoryImpl(
            authApi = object : AuthApi by unusedAuthApi() {
                override suspend fun register(body: RegisterRequestDto): UserDto {
                    throw CancellationException("cancelled")
                }
            },
            usersApi = unusedUsersApi(),
            tokenStore = newTokenStore()
        )

        assertThrows(CancellationException::class.java) {
            runBlocking {
                repository.register("user@example.com", "Strong1!", Role.MAHASISWA)
            }
        }
    }

    private fun unusedAuthApi() = object : AuthApi {
        override suspend fun register(body: RegisterRequestDto): UserDto =
            error("Unused")

        override suspend fun login(body: LoginRequestDto): LoginResponseDto =
            error("Unused")

        override suspend fun googleSignIn(body: GoogleSignInRequestDto): AuthTokensDto =
            error("Unused")

        override suspend fun verifyEmail(body: VerifyEmailRequestDto): VerifyEmailResponseDto =
            error("Unused")

        override suspend fun resendVerification(body: ResendVerificationRequestDto): ResendVerificationResponseDto =
            error("Unused")

        override suspend fun forgotPassword(body: ForgotPasswordRequestDto): ForgotPasswordResponseDto =
            error("Unused")

        override suspend fun resetPassword(body: ResetPasswordRequestDto): ResetPasswordResponseDto =
            error("Unused")

        override suspend fun changePassword(
            body: com.app.garapan.data.remote.dto.ChangePasswordRequestDto
        ): com.app.garapan.data.remote.dto.ChangePasswordResponseDto =
            error("Unused")

        override suspend fun refresh(body: RefreshRequestDto): AuthTokensDto =
            error("Unused")

        override suspend fun logout(body: LogoutRequestDto): LogoutResponseDto =
            error("Unused")

        override suspend fun verifyTwoFactor(body: TwoFactorVerifyRequestDto): AuthTokensDto =
            error("Unused")

        override suspend fun resendTwoFactor(body: ResendTwoFactorRequestDto): ResendTwoFactorResponseDto =
            error("Unused")
    }

    private fun unusedUsersApi() = object : UsersApi {
        override suspend fun getMe(): UserDto =
            error("Unused")

        override suspend fun updateMe(body: UpdateProfileRequestDto): UserDto =
            error("Unused")

        override suspend fun uploadAvatar(image: okhttp3.MultipartBody.Part): UserDto =
            error("Unused")

        override suspend fun getPublicProfile(id: String): com.app.garapan.data.remote.dto.PublicUserDto =
            error("Unused")
    }

    private fun newTokenStore(): AuthTokenStore {
        val file = Files.createTempDirectory("garapan-repository-test")
            .resolve("preferences.preferences_pb")
            .toFile()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { file }
        )
        return AuthTokenStore(dataStore)
    }
}
