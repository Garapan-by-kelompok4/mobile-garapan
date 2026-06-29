package com.app.garapan.data.mapper

import com.app.garapan.data.remote.dto.KlienProfileDto
import com.app.garapan.data.remote.dto.MahasiswaProfileDto
import com.app.garapan.data.remote.dto.SocialAccountsDto
import com.app.garapan.data.remote.dto.UpdateProfileRequestDto
import com.app.garapan.data.remote.dto.UserDto
import com.app.garapan.data.remote.dto.AuthTokensDto
import com.app.garapan.data.remote.dto.LoginResponseDto
import com.app.garapan.domain.model.AuthTokens
import com.app.garapan.domain.model.KlienProfile
import com.app.garapan.domain.model.LoginResult
import com.app.garapan.domain.model.MahasiswaProfile
import com.app.garapan.domain.model.ProfileStatus
import com.app.garapan.domain.model.Role
import com.app.garapan.domain.model.SocialAccounts
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
    klien = klien?.toDomain(),
    displayName = displayName,
    phoneNumber = phoneNumber,
    status = ProfileStatus.fromApiValue(status),
    socialAccounts = socialAccounts?.toDomain() ?: SocialAccounts()
)

fun MahasiswaProfileDto.toDomain() = MahasiswaProfile(
    id = id,
    userId = userId,
    university = university,
    skills = skills,
    bio = bio,
    walletBalance = walletBalance,
    rating = rating,
    suggestedKategoriId = suggestedKategoriId,
    suggestedKategoriName = suggestedKategori?.name.orEmpty(),
    fullName = fullName,
    avatarUrl = avatarUrl
)

fun KlienProfileDto.toDomain() = KlienProfile(
    id = id,
    userId = userId,
    companyName = companyName,
    bio = bio,
    walletBalance = walletBalance,
    avatarUrl = avatarUrl
)

fun SocialAccountsDto.toDomain() = SocialAccounts(
    linkedinUrl = linkedinUrl
)

fun UpdateProfileParams.toDto() = UpdateProfileRequestDto(
    displayName = displayName,
    university = university,
    skills = skills,
    bio = bio,
    companyName = companyName,
    phoneNumber = phoneNumber,
    status = status?.apiValue,
    socialAccounts = linkedinUrl?.let { SocialAccountsDto(linkedinUrl = it) },
    deviceToken = deviceToken
)
