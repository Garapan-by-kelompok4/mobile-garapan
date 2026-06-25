package com.app.garapan.domain.model

data class PortofolioImage(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PortofolioImage) return false
        return bytes.contentEquals(other.bytes) &&
            mimeType == other.mimeType &&
            fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}
