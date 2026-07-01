package com.app.garapan.data.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object ChatAttachmentReader {
    const val MAX_BYTES = 10_000_000

    class DocumentAttachment(
        val bytes: ByteArray,
        val fileName: String,
        val mimeType: String
    )

    /** Reads a picked document Uri as-is (no compression). Returns null if unreadable or over [MAX_BYTES]. */
    fun readDocument(context: Context, uri: Uri): DocumentAttachment? {
        val bytes = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()?.takeIf { it.isNotEmpty() && it.size <= MAX_BYTES } ?: return null

        val fileName = resolveDisplayName(context, uri)
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        return DocumentAttachment(bytes, fileName, mimeType)
    }

    private fun resolveDisplayName(context: Context, uri: Uri): String {
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        }.getOrNull()?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { return it }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "attachment"
    }
}
