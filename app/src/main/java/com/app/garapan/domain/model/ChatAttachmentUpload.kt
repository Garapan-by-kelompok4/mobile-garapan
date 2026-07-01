package com.app.garapan.domain.model

/** A file picked on-device, ready to upload as a chat attachment. */
class ChatAttachmentUpload(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String
)
