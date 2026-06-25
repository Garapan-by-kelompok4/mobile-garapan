package com.app.garapan.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.app.garapan.domain.model.PortofolioImage
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.roundToInt

object PortfolioImageReader {
    const val MAX_BYTES = 5_000_000
    private const val MAX_DIMENSION = 1280
    private const val JPEG_QUALITY = 76
    private const val UPLOAD_TARGET_BYTES = 300_000

    fun readUriBytes(context: Context, uri: Uri): ByteArray? {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { return it }

        return runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
            }
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    fun readCompressed(context: Context, uri: Uri): PortofolioImage? =
        when (val result = readCompressedWithResult(context, uri)) {
            is PortfolioImageReadResult.Success -> result.image
            is PortfolioImageReadResult.Failure -> null
        }

    fun readCompressedWithResult(context: Context, uri: Uri): PortfolioImageReadResult {
        val fileName = resolveFileName(uri)

        val decoded = readWithBitmapFactory(context, uri)
            ?: decodeWithImageDecoder(context, uri)
        if (decoded != null) {
            return compressBitmap(decoded, fileName)?.let { PortfolioImageReadResult.Success(it) }
                ?: PortfolioImageReadResult.Failure(stage = "compress", detail = "output_invalid")
        }

        val rawBytes = readUriBytes(context, uri)
            ?: return PortfolioImageReadResult.Failure(
                stage = "decode",
                detail = "uri_unreadable"
            )
        return readCompressedFromBytes(rawBytes, fileName, context, uri)
    }

    fun readCompressedFromBytes(
        bytes: ByteArray,
        fileName: String,
        context: Context? = null,
        uri: Uri? = null
    ): PortfolioImageReadResult {
        if (bytes.isEmpty()) {
            return PortfolioImageReadResult.Failure(stage = "read", detail = "empty_bytes")
        }

        compressBytes(bytes, fileName)?.let { return PortfolioImageReadResult.Success(it) }

        decodeBytesWithImageDecoder(bytes)?.let { decoded ->
            compressBitmap(decoded, fileName)?.let { return PortfolioImageReadResult.Success(it) }
            return PortfolioImageReadResult.Failure(stage = "compress", detail = "output_invalid")
        }

        if (context != null && uri != null) {
            val decoded = decodeWithImageDecoder(context, uri)
            if (decoded != null) {
                compressBitmap(decoded, fileName)?.let { return PortfolioImageReadResult.Success(it) }
                return PortfolioImageReadResult.Failure(stage = "compress", detail = "output_invalid")
            }
        }

        return PortfolioImageReadResult.Failure(stage = "decode", detail = "byte_decode_failed")
    }

    fun compressBytes(bytes: ByteArray, fileName: String = "portfolio.jpg"): PortofolioImage? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null
        return compressBitmap(decoded, fileName)
    }

    private fun readWithBitmapFactory(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = resolver.openInputStream(uri) ?: return null
        boundsStream.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decodeStream = resolver.openInputStream(uri) ?: return null
        return decodeStream.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    }

    private fun decodeBytesWithImageDecoder(bytes: ByteArray): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return try {
            val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
            decodeWithImageDecoderConfig(source)
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeWithImageDecoder(context: Context, uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            decodeWithImageDecoderConfig(source)
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeWithImageDecoderConfig(source: ImageDecoder.Source): Bitmap {
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val width = info.size.width
            val height = info.size.height
            val sampleSize = calculateSampleSize(width, height, MAX_DIMENSION)
            decoder.setTargetSize(
                (width / sampleSize).coerceAtLeast(1),
                (height / sampleSize).coerceAtLeast(1)
            )
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
    }

    private fun resolveFileName(uri: Uri): String {
        return uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() }
            ?.let { if (it.contains('.')) it.substringBeforeLast('.') + ".jpg" else "$it.jpg" }
            ?: "portfolio.jpg"
    }

    private fun compressBitmap(bitmap: Bitmap, fileName: String): PortofolioImage? {
        val softwareBitmap = toSoftwareBitmap(bitmap)
        if (softwareBitmap !== bitmap) {
            bitmap.recycle()
        }

        val scaled = scaleDownIfNeeded(softwareBitmap, MAX_DIMENSION)
        if (scaled !== softwareBitmap) {
            softwareBitmap.recycle()
        }

        var quality = JPEG_QUALITY
        var output = ByteArray(0)
        var compressSucceeded = false
        while (quality >= 50) {
            ByteArrayOutputStream().use { stream ->
                compressSucceeded = scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                output = stream.toByteArray()
            }
            if (!compressSucceeded) break
            if (output.size <= UPLOAD_TARGET_BYTES) break
            quality -= 10
        }

        if (!compressSucceeded || output.isEmpty() || output.size > MAX_BYTES) {
            scaled.recycle()
            return null
        }
        scaled.recycle()
        return PortofolioImage(bytes = output, mimeType = "image/jpeg", fileName = fileName)
    }

    private fun toSoftwareBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.config != Bitmap.Config.HARDWARE) return bitmap
        return bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: bitmap
    }

    private fun scaleDownIfNeeded(source: Bitmap, maxDimension: Int): Bitmap {
        val largestSide = max(source.width, source.height)
        if (largestSide <= maxDimension) return source

        val scale = maxDimension.toFloat() / largestSide
        val targetWidth = (source.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (source.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    private fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sampleSize = 1
        val largestSide = max(width, height)
        while (largestSide / sampleSize > maxDimension * 2) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
