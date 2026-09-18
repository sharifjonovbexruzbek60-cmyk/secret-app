package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

data class CompressedImageResult(
    val fileUri: String,
    val originalSize: Long,
    val compressedSize: Long,
    val width: Int,
    val height: Int
)

data class VideoInfoResult(
    val fileUri: String,
    val thumbnailUri: String?,
    val durationMs: Long,
    val sizeBytes: Long
)

object MediaUtils {
    private const val TAG = "MediaUtils"
    private const val MAX_IMAGE_DIMENSION = 1024
    private const val JPEG_COMPRESSION_QUALITY = 70

    /**
     * Auto-compresses an image from Uri for low-end Android devices.
     * Uses inSampleSize downsampling and JPEG 70 compression to drastically reduce RAM & storage.
     */
    suspend fun compressImage(context: Context, sourceUri: Uri): CompressedImageResult = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val originalSize = runCatching {
            contentResolver.openFileDescriptor(sourceUri, "r")?.use { it.statSize } ?: 0L
        }.getOrDefault(0L)

        // Step 1: Decode image dimensions without loading full bitmap into memory
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }

        val origWidth = options.outWidth
        val origHeight = options.outHeight

        // Step 2: Calculate inSampleSize to save memory on low-end devices
        var inSampleSize = 1
        if (origHeight > MAX_IMAGE_DIMENSION || origWidth > MAX_IMAGE_DIMENSION) {
            val halfHeight = origHeight / 2
            val halfWidth = origWidth / 2
            while ((halfHeight / inSampleSize) >= MAX_IMAGE_DIMENSION &&
                (halfWidth / inSampleSize) >= MAX_IMAGE_DIMENSION
            ) {
                inSampleSize *= 2
            }
        }

        // Step 3: Decode bitmap with sample size & RGB_565 (uses 2 bytes per pixel instead of 4)
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }

        val decodedBitmap: Bitmap? = contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        }

        val finalBitmap: Bitmap = if (decodedBitmap != null) {
            // Further scale if still larger than max dimension
            if (decodedBitmap.width > MAX_IMAGE_DIMENSION || decodedBitmap.height > MAX_IMAGE_DIMENSION) {
                val ratio = minOf(
                    MAX_IMAGE_DIMENSION.toFloat() / decodedBitmap.width,
                    MAX_IMAGE_DIMENSION.toFloat() / decodedBitmap.height
                )
                val targetW = (decodedBitmap.width * ratio).roundToInt().coerceAtLeast(1)
                val targetH = (decodedBitmap.height * ratio).roundToInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(decodedBitmap, targetW, targetH, true)
                if (scaled != decodedBitmap) {
                    decodedBitmap.recycle()
                }
                scaled
            } else {
                decodedBitmap
            }
        } else {
            // Fallback placeholder bitmap
            Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565)
        }

        // Step 4: Save compressed image to internal files directory
        val mediaDir = File(context.filesDir, "chat_images").apply { mkdirs() }
        val outputFile = File(mediaDir, "img_${System.currentTimeMillis()}.jpg")

        FileOutputStream(outputFile).use { outStream ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, outStream)
        }

        val finalWidth = finalBitmap.width
        val finalHeight = finalBitmap.height
        finalBitmap.recycle()

        val compressedSize = outputFile.length()

        CompressedImageResult(
            fileUri = outputFile.absolutePath,
            originalSize = if (originalSize > 0) originalSize else compressedSize,
            compressedSize = compressedSize,
            width = finalWidth,
            height = finalHeight
        )
    }

    /**
     * Extracts thumbnail, duration and copies video locally.
     */
    suspend fun processVideo(context: Context, sourceUri: Uri): VideoInfoResult = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val mediaDir = File(context.filesDir, "chat_videos").apply { mkdirs() }
        val outputFile = File(mediaDir, "vid_${System.currentTimeMillis()}.mp4")

        // Copy video file to app internal storage
        contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        var durationMs = 0L
        var thumbnailPath: String? = null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(outputFile.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durationStr?.toLongOrNull() ?: 0L

            // Extract frame at 1s or beginning
            val thumbBitmap = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime

            thumbBitmap?.let { bitmap ->
                val thumbDir = File(context.filesDir, "chat_thumbnails").apply { mkdirs() }
                val thumbFile = File(thumbDir, "thumb_${System.currentTimeMillis()}.jpg")
                FileOutputStream(thumbFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
                }
                bitmap.recycle()
                thumbnailPath = thumbFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed extracting video metadata", e)
        } finally {
            runCatching { retriever.release() }
        }

        VideoInfoResult(
            fileUri = outputFile.absolutePath,
            thumbnailUri = thumbnailPath,
            durationMs = durationMs,
            sizeBytes = outputFile.length()
        )
    }

    /**
     * Formats bytes into clean human-readable size (e.g. 45 KB, 1.2 MB).
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Formats milliseconds to mm:ss format.
     */
    fun formatDuration(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    /**
     * The Shredder Protocol:
     * Overwrites file data with zeroes across storage blocks before unlinking from the file system.
     * Prevents forensic recovery of burn-after-reading images, videos, and voice notes.
     */
    fun zeroFillAndShred(file: File?): Boolean {
        if (file == null || !file.exists() || !file.isFile) return false
        return try {
            val length = file.length()
            if (length > 0) {
                java.io.RandomAccessFile(file, "rws").use { raf ->
                    raf.seek(0)
                    val buffer = ByteArray(4096.coerceAtMost(length.toInt()))
                    var written = 0L
                    while (written < length) {
                        val chunk = minOf(buffer.size.toLong(), length - written).toInt()
                        raf.write(buffer, 0, chunk)
                        written += chunk
                    }
                }
            }
            file.delete()
        } catch (e: Exception) {
            file.delete()
        }
    }
}
