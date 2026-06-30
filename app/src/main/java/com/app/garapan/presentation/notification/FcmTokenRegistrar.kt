package com.app.garapan.presentation.notification

import android.content.Context
import android.util.Log
import com.app.garapan.domain.common.Resource
import com.app.garapan.domain.usecase.RegisterFcmTokenUseCase
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase
) {
    fun registerCurrentToken(scope: CoroutineScope) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.w(TAG, "FirebaseApp is not initialized; skipping FCM token registration")
            return
        }

        Log.i(TAG, "Requesting current FCM token")
        runCatching { FirebaseMessaging.getInstance().token }
            .onSuccess { task ->
                task.addOnCompleteListener { completed ->
                    if (!completed.isSuccessful) {
                        Log.w(TAG, "Failed to read FCM token", completed.exception)
                        return@addOnCompleteListener
                    }
                    val token = completed.result.orEmpty()
                    Log.i(TAG, "Received FCM token (${token.length} chars); registering with backend")
                    scope.launch { registerToken(token) }
                }
            }
            .onFailure { error ->
                Log.w(TAG, "Firebase is not ready for FCM token registration", error)
            }
    }

    suspend fun registerToken(token: String) {
        repeat(MAX_REGISTER_ATTEMPTS) { attempt ->
            when (val result = registerFcmTokenUseCase(token)) {
                is Resource.Success -> {
                    Log.i(TAG, "FCM token registered with backend")
                    return
                }
                is Resource.Error -> {
                    Log.w(
                        TAG,
                        "Failed to register FCM token (attempt ${attempt + 1}/$MAX_REGISTER_ATTEMPTS): ${result.message}"
                    )
                    if (attempt < MAX_REGISTER_ATTEMPTS - 1) {
                        delay(RETRY_DELAY_MS)
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private companion object {
        const val TAG = "FcmTokenRegistrar"
        const val MAX_REGISTER_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 5_000L
    }
}
