package com.app.garapan.data.remote.dto

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val accessToken: String?,
    val refreshToken: String?,
    val requiresTwoFactor: Boolean?,
    val preAuthToken: String?
)

data class VerifyEmailRequestDto(val token: String)
data class VerifyEmailResponseDto(val emailVerified: Boolean)

data class ResendVerificationRequestDto(val email: String)
data class ResendVerificationResponseDto(val verificationSent: Boolean)

data class RefreshRequestDto(val refreshToken: String)

data class AuthTokensDto(
    val accessToken: String,
    val refreshToken: String
)

data class LogoutRequestDto(val refreshToken: String)
data class LogoutResponseDto(val loggedOut: Boolean)

data class TwoFactorVerifyRequestDto(
    val preAuthToken: String,
    val otp: String
)

data class ResendTwoFactorRequestDto(val preAuthToken: String)
data class ResendTwoFactorResponseDto(val otpSent: Boolean)
