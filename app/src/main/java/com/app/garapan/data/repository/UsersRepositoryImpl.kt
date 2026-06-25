package com.app.garapan.data.repository

import com.app.garapan.data.mapper.toPublicProfile
import com.app.garapan.data.remote.api.UsersApi
import com.app.garapan.data.remote.error.ApiErrorMapper
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.model.PublicProfile
import com.app.garapan.domain.repository.UsersRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi
) : UsersRepository {

    override suspend fun getPublicProfile(userId: String): Resource<PublicProfile> =
        safeApiCall {
            usersApi.getPublicProfile(userId).toPublicProfile()
        }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> =
        try {
            Resource.Success(block())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Resource.Error(ApiErrorMapper.toMessage(throwable))
        }
}
