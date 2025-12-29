package me.restarhalf.deer.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

private object RemoteBitmapLoader {
    private val client = OkHttpClient()

    private const val MAX_DIMENSION = 512

    private val cache = object : LruCache<String, Bitmap>(20 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private fun md5Hex(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(((b.toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }

    private fun cacheFile(context: Context, url: String): File {
        val dir = File(context.cacheDir, "avatar_cache")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, md5Hex(url))
    }

    private fun readFromDisk(context: Context, url: String): Bitmap? {
        val file = cacheFile(context, url)
        if (!file.exists()) return null

        val bitmap = decodeBitmapFile(file.absolutePath)
        if (bitmap == null) {
            file.delete()
        }
        return bitmap
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        if (height <= 0 || width <= 0) return 1

        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun decodeBitmapBytes(bytes: ByteArray): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            opts.inSampleSize = calculateInSampleSize(opts, MAX_DIMENSION, MAX_DIMENSION)
            opts.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeBitmapFile(path: String): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, opts)
            opts.inSampleSize = calculateInSampleSize(opts, MAX_DIMENSION, MAX_DIMENSION)
            opts.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, opts)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun writeToDisk(context: Context, url: String, bytes: ByteArray) {
        val file = cacheFile(context, url)
        val tmp = File(file.absolutePath + ".tmp")

        try {
            tmp.outputStream().use { it.write(bytes) }
            if (file.exists()) file.delete()
            tmp.renameTo(file)
        } catch (_: Exception) {
            tmp.delete()
        }
    }

    fun get(url: String): Bitmap? = cache.get(url)

    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    suspend fun load(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        get(url)?.let { return@withContext it }

        readFromDisk(context, url)?.let { bitmap ->
            put(url, bitmap)
            return@withContext bitmap
        }

        val request = try {
            Request.Builder()
                .url(url)
                .build()
        } catch (_: IllegalArgumentException) {
            return@withContext null
        }

        try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null

                val bytes = resp.body.bytes()
                val bitmap = decodeBitmapBytes(bytes)
                    ?: return@withContext null

                writeToDisk(context, url, bytes)
                put(url, bitmap)
                bitmap
            }
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun rememberRemoteImageBitmap(url: String?): State<ImageBitmap?> {
    val context = LocalContext.current.applicationContext

    return produceState<ImageBitmap?>(initialValue = null, url) {
        val cleaned = url?.trim()?.takeIf { it.isNotBlank() }
        if (cleaned == null) {
            value = null
            return@produceState
        }

        if (!(cleaned.startsWith("http://") || cleaned.startsWith("https://"))) {
            value = null
            return@produceState
        }

        val cached = RemoteBitmapLoader.get(cleaned)
        if (cached != null) {
            value = cached.asImageBitmap()
            return@produceState
        }

        value = RemoteBitmapLoader.load(context, cleaned)?.asImageBitmap()
    }
}

@Composable
fun AvatarCircle(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentDescription: String? = null,
    fallback: @Composable () -> Unit
) {
    val bitmap by rememberRemoteImageBitmap(avatarUrl)

    val imageBitmap = bitmap

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            fallback()
        }
    }
}
