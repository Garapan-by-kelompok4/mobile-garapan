package com.app.garapan.data.repository

import com.app.garapan.data.local.AuthTokenStore
import com.app.garapan.data.mapper.toDomain
import com.app.garapan.data.mapper.toDto
import com.app.garapan.data.remote.api.AuthApi
import com.app.garapan.data.remote.api.UsersApi
import com.app.garapan.data.remote.dto.LoginRequestDto
import com.app.garapan.data.remote.dto.LogoutRequestDto
import com.app.garapan.data.remote.dto.RefreshRequestDto
import com.app.garapan.data.remote.dto.RegisterRequestDto
import com.app.garapan.data.remote.dto.ResendVerificationRequestDto
import com.app.garapan.data.remote.dto.VerifyEmailRequestDto
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User
import com.app.garapan.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val usersApi: UsersApi,
    private val tokenStore: AuthTokenStore
) : AuthRepository {

    override suspend fun getAccessToken(): String? =
        tokenStore.getAccessToken()

    override suspend fun getRefreshToken(): String? =
        tokenStore.getRefreshToken()

    override suspend fun saveAuthTokens(tokens: AuthTokens) {
        tokenStore.saveTokens(tokens)
    }

    override suspend fun clearAuthTokens() {
        tokenStore.clearTokens()
    }

    override suspend fun register(email: String, password: String, role: Role): Resource<User> =
        safeApiCall {
            authApi.register(RegisterRequestDto(email = email, password = password, role = role.name))
                .toDomain()
        }

    override suspend fun login(email: String, password: String): Resource<LoginResult> =
        safeApiCall {
            val result = authApi.login(LoginRequestDto(email = email, password = password)).toDomain()
            if (result is LoginResult.Authenticated) {
                tokenStore.saveTokens(result.tokens)
            }
            result
        }

    override suspend fun verifyEmail(token: String): Resource<Boolean> =
        safeApiCall { authApi.verifyEmail(VerifyEmailRequestDto(token)).emailVerified }

    override suspend fun resendVerification(email: String): Resource<Boolean> =
        safeApiCall { authApi.resendVerification(ResendVerificationRequestDto(email)).verificationSent }

    override suspend fun refresh(): Resource<AuthTokens> =
        safeApiCall {
            val refreshToken = tokenStore.getRefreshToken()
                ?: error("No refresh token available")
            val tokens = authApi.refresh(RefreshRequestDto(refreshToken)).toDomain()
            tokenStore.saveTokens(tokens)
            tokens
        }

    override suspend fun logout(): Resource<Boolean> {
        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            tokenStore.clearTokens()
            return Resource.Success(true)
        }

        return when (val result = safeApiCall { authApi.logout(LogoutRequestDto(refreshToken)).loggedOut }) {
            is Resource.Success -> {
                tokenStore.clearTokens()
                result
            }
            is Resource.Error -> {
                tokenStore.clearTokens()
                result
            }
            Resource.Loading -> Resource.Loading
        }
    }

    override suspend fun getMe(): Resource<User> =
        safeApiCall { usersApi.getMe().toDomain() }

    override suspend fun updateProfile(params: UpdateProfileParams): Resource<User> =
        safeApiCall { usersApi.updateMe(params.toDto()).toDomain() }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
