package com.app.garapan.domain.repository

import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.PublicProfile

interface UsersRepository {
    suspend fun getPublicProfile(userId: String): Resource<PublicProfile>
}
