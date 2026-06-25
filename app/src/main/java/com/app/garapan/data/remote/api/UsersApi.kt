package com.app.garapan.data.remote.api

import com.app.garapan.data.remote.dto.PublicUserDto
import com.app.garapan.data.remote.dto.UpdateProfileRequestDto
import com.app.garapan.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UsersApi {
    @GET("users/me")
    suspend fun getMe(): UserDto

    @PATCH("users/me")
    suspend fun updateMe(@Body body: UpdateProfileRequestDto): UserDto

    @GET("users/{id}")
    suspend fun getPublicProfile(@Path("id") id: String): PublicUserDto
}
