package com.app.garapan.data.remote.error

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorMapper {
    fun toMessage(throwable: Throwable): String =
        when (throwable) {
            is HttpException -> {
                if (throwable.code() == 429) {
                    "Too many attempts. Please wait a minute and try again."
                } else {
                    throwable.response()?.errorBody()?.string()
                        ?.let(::parseErrorBody)
                        ?: "Request failed. Please try again."
                }
            }
            is SocketTimeoutException ->
                "Upload took too long. Try again on a stable connection or use a smaller image."
            is UnknownHostException ->
                "Unable to connect. Check your internet connection and try again."
            is IOException ->
                "Network error while uploading. Check your connection and try again."
            else -> throwable.message?.takeIf { it.isNotBlank() }
                ?: "Something went wrong. Please try again."
        }

    private fun parseErrorBody(body: String): String =
        runCatching {
            val root = JsonParser.parseString(body).asJsonObject
            val message = root.get("message")
            when {
                message == null || message.isJsonNull -> root.get("error")?.asString
                message.isJsonArray -> message.asJsonArray
                    .mapNotNull { it.takeIf { value -> value.isJsonPrimitive }?.asString }
                    .joinToString(separator = "\n")
                message.isJsonPrimitive -> message.asString
                else -> null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Request failed. Please try again."
}
