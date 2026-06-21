package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.AuthTokensDto
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
import com.app.garapan.data.remote.dto.TwoFactorVerifyRequestDto
import com.app.garapan.data.remote.dto.UserDto
import com.app.garapan.data.remote.dto.VerifyEmailRequestDto
import com.app.garapan.data.remote.dto.VerifyEmailResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): UserDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequestDto): VerifyEmailResponseDto

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body body: ResendVerificationRequestDto): ResendVerificationResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): AuthTokensDto

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): LogoutResponseDto

    @POST("auth/2fa/verify")
    suspend fun verifyTwoFactor(@Body body: TwoFactorVerifyRequestDto): AuthTokensDto

    @POST("auth/2fa/resend")
    suspend fun resendTwoFactor(@Body body: ResendTwoFactorRequestDto): ResendTwoFactorResponseDto
}
