package com.app.garapan.domain.model

sealed interface LoginResult {
    data class Authenticated(val tokens: AuthTokens) : LoginResult
    data class RequiresTwoFactor(val preAuthToken: String) : LoginResult
}
