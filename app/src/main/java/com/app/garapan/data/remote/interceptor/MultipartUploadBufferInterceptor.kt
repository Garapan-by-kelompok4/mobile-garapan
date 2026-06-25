package com.app.garapan.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer

/**
 * Buffers multipart upload bodies before they hit the socket so the server receives
 * a complete Content-Length payload in one shot.
 */
class MultipartUploadBufferInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val body = request.body
        if (body == null || request.method != "POST" || !request.url.encodedPath.endsWith(PORTFOLIO_UPLOAD_PATH)) {
            return chain.proceed(request)
        }
        if (body !is MultipartBody) {
            return chain.proceed(request)
        }

        val buffer = Buffer()
        body.writeTo(buffer)
        val bufferedBody = buffer.readByteArray().toRequestBody(body.contentType())

        return chain.proceed(
            request.newBuilder()
                .method(request.method, bufferedBody)
                .build()
        )
    }

    private companion object {
        const val PORTFOLIO_UPLOAD_PATH = "/portofolio"
    }
}
