package com.app.garapan.domain.repository

interface AuthRepository {
    suspend fun getAuthToken(): String?
    suspend fun saveAuthToken(token: String)
    suspend fun clearAuthToken()
}
