package com.app.garapan.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.app.garapan.domain.model.AuthTokens
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class TokenSnapshot(
    val accessToken: String?,
    val refreshToken: String?
)

@Singleton
class AuthTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    @Volatile
    private var cachedTokens: TokenSnapshot? = null

    @Volatile
    private var cacheLoaded: Boolean = false

    fun getCachedAccessToken(): String? =
        cachedTokens?.accessToken

    fun getCachedRefreshToken(): String? =
        cachedTokens?.refreshToken

    fun isCacheLoaded(): Boolean = cacheLoaded

    suspend fun getAccessToken(): String? =
        getTokenSnapshot().accessToken

    suspend fun getRefreshToken(): String? =
        getTokenSnapshot().refreshToken

    suspend fun saveTokens(tokens: AuthTokens) {
        dataStore.edit {
            it[ACCESS_TOKEN_KEY] = TokenCrypto.encrypt(tokens.accessToken)
            it[REFRESH_TOKEN_KEY] = TokenCrypto.encrypt(tokens.refreshToken)
        }
        cachedTokens = TokenSnapshot(tokens.accessToken, tokens.refreshToken)
        cacheLoaded = true
    }

    suspend fun clearTokens() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
            it.remove(LEGACY_AUTH_TOKEN_KEY)
        }
        cachedTokens = TokenSnapshot(accessToken = null, refreshToken = null)
        cacheLoaded = true
    }

    private suspend fun getTokenSnapshot(): TokenSnapshot {
        if (cacheLoaded) {
            return cachedTokens ?: TokenSnapshot(accessToken = null, refreshToken = null)
        }

        return dataStore.data.first().let { preferences ->
            val accessToken = preferences[ACCESS_TOKEN_KEY]?.let(TokenCrypto::decrypt)
            val refreshToken = preferences[REFRESH_TOKEN_KEY]?.let(TokenCrypto::decrypt)
            if ((preferences[ACCESS_TOKEN_KEY] != null && accessToken == null) ||
                (preferences[REFRESH_TOKEN_KEY] != null && refreshToken == null)
            ) {
                clearTokens()
                TokenSnapshot(accessToken = null, refreshToken = null)
            } else {
                TokenSnapshot(accessToken = accessToken, refreshToken = refreshToken)
            }
        }.also { snapshot ->
            cachedTokens = snapshot
            cacheLoaded = true
        }
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val LEGACY_AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}

private object TokenCrypto {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "garapan_auth_tokens"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ENCRYPTED_PREFIX = "v1"
    private const val GCM_TAG_LENGTH_BITS = 128

    fun encrypt(plainText: String): String {
        val secretKey = getOrCreateSecretKey() ?: return plainText
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = Base64.getEncoder().encodeToString(cipher.iv)
            val cipherText = Base64.getEncoder().encodeToString(
                cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            )
            "$ENCRYPTED_PREFIX:$iv:$cipherText"
        }.getOrElse { plainText }
    }

    fun decrypt(storedValue: String): String? {
        val parts = storedValue.split(":", limit = 3)
        if (parts.size != 3 || parts[0] != ENCRYPTED_PREFIX) {
            return null
        }
        val secretKey = getOrCreateSecretKey() ?: return null
        return runCatching {
            val iv = Base64.getDecoder().decode(parts[1])
            val cipherText = Base64.getDecoder().decode(parts[2])
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        }.getOrNull()
    }

    private fun getOrCreateSecretKey(): SecretKey? =
        runCatching {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            (keyStore.getKey(KEY_ALIAS, null) as? SecretKey) ?: createSecretKey()
        }.getOrNull()

    private fun createSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
