package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User

interface AuthRepository {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveAuthTokens(tokens: AuthTokens)
    suspend fun clearAuthTokens()

    suspend fun register(email: String, password: String, role: Role): Resource<User>
    suspend fun login(email: String, password: String): Resource<LoginResult>
    suspend fun googleSignIn(idToken: String, role: Role?): Resource<AuthTokens>
    suspend fun verifyTwoFactor(preAuthToken: String, otp: String): Resource<AuthTokens>
    suspend fun resendOtp(preAuthToken: String): Resource<Boolean>
    suspend fun verifyEmail(token: String): Resource<Boolean>
    suspend fun resendVerification(email: String): Resource<Boolean>
    suspend fun forgotPassword(email: String): Resource<Boolean>
    suspend fun resetPassword(token: String, newPassword: String): Resource<Boolean>
    suspend fun refresh(): Resource<AuthTokens>
    suspend fun logout(): Resource<Boolean>
    suspend fun getMe(): Resource<User>
    suspend fun updateProfile(params: UpdateProfileParams): Resource<User>
}
