package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.AuthTokensDto
import com.app.garapan.data.remote.dto.KlienProfileDto
import com.app.garapan.data.remote.dto.LoginResponseDto
import com.app.garapan.data.remote.dto.MahasiswaProfileDto
import com.app.garapan.data.remote.dto.UpdateProfileRequestDto
import com.app.garapan.data.remote.dto.UserDto
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.KlienProfile
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.MahasiswaProfile
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.UpdateProfileParams
import com.app.garapan.domain.model.User

fun AuthTokensDto.toDomain() = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun LoginResponseDto.toDomain(): LoginResult =
    if (requiresTwoFactor == true) {
        LoginResult.RequiresTwoFactor(preAuthToken = preAuthToken.orEmpty())
    } else {
        LoginResult.Authenticated(
            AuthTokens(
                accessToken = requireNotNull(accessToken) { "Missing access token" },
                refreshToken = requireNotNull(refreshToken) { "Missing refresh token" }
            )
        )
    }

fun UserDto.toDomain() = User(
    id = id,
    email = email,
    role = Role.valueOf(role),
    emailVerified = emailVerified,
    deviceToken = deviceToken,
    twoFactorEnabled = twoFactorEnabled,
    createdAt = createdAt,
    updatedAt = updatedAt,
    mahasiswa = mahasiswa?.toDomain(),
    klien = klien?.toDomain()
)

fun MahasiswaProfileDto.toDomain() = MahasiswaProfile(
    id = id,
    userId = userId,
    university = university,
    skills = skills,
    bio = bio,
    walletBalance = walletBalance,
    rating = rating
)

fun KlienProfileDto.toDomain() = KlienProfile(
    id = id,
    userId = userId,
    companyName = companyName,
    bio = bio,
    walletBalance = walletBalance
)

fun UpdateProfileParams.toDto() = UpdateProfileRequestDto(
    university = university,
    skills = skills,
    bio = bio,
    companyName = companyName,
    deviceToken = deviceToken
)
