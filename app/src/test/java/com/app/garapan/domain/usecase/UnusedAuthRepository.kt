package com.app.garapan.domain.usecase

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.PortofolioImage
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository

internal class UnusedAuthRepository : AuthRepository {
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
    override suspend fun refresh(): Resource<AuthTokens> = unused()
    override suspend fun logout(): Resource<Boolean> = unused()
    override suspend fun getMe(): Resource<User> = unused()
    override suspend fun updateProfile(params: UpdateProfileParams): Resource<User> = unused()
    override suspend fun uploadAvatar(image: PortofolioImage): Resource<User> = unused()

    private fun unused(): Nothing = error("unused")
}
