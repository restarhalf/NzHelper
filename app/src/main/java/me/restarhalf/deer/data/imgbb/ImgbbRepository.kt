package me.restarhalf.deer.data.imgbb

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.restarhalf.deer.BuildConfig
import me.restarhalf.deer.data.supabase.SupabaseHttp
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ImgbbApiException(
    val statusCode: Int,
    val errorBody: String?,
    message: String
) : Exception(message)

data class ImgbbUploadResponse(
    @param:Json(name = "data") val data: ImgbbUploadData?,
    @param:Json(name = "success") val success: Boolean?,
    @param:Json(name = "status") val status: Int?
)

data class ImgbbUploadData(
    @param:Json(name = "url") val url: String?,
    @param:Json(name = "display_url") val displayUrl: String?
)

object ImgbbRepository {
    private val client = OkHttpClient()
    private val uploadAdapter = SupabaseHttp.moshi.adapter(ImgbbUploadResponse::class.java)

    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        fileName: String = "avatar"
    ): String {
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } ?: throw IllegalArgumentException("Failed to read image")

        return uploadImageBytes(
            imageBytes = bytes,
            fileName = fileName
        )
    }

    suspend fun uploadImageBytes(
        imageBytes: ByteArray,
        fileName: String = "avatar"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.IMGBB_API_KEY.trim()
        if (apiKey.isBlank()) {
            throw IllegalStateException("IMGBB_API_KEY is empty. Please set it in .env")
        }

        val url = "https://api.imgbb.com/1/upload".toHttpUrl().newBuilder()
            .addQueryParameter("key", apiKey)
            .build()

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "$fileName.jpg",
                imageBytes.toRequestBody("application/octet-stream".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(multipart)
            .build()

        val resp = client.newCall(request).execute()
        val body = resp.body.string().orEmpty()

        if (!resp.isSuccessful) {
            throw ImgbbApiException(resp.code, body, "IMGBB HTTP ${resp.code}")
        }

        val parsed = uploadAdapter.fromJson(body)
            ?: throw ImgbbApiException(resp.code, body, "Invalid IMGBB response")

        if (parsed.success != true) {
            throw ImgbbApiException(resp.code, body, "IMGBB upload failed")
        }

        val imageUrl = parsed.data?.displayUrl
            ?.takeIf { it.isNotBlank() }
            ?: parsed.data?.url?.takeIf { it.isNotBlank() }

        imageUrl ?: throw ImgbbApiException(resp.code, body, "IMGBB response missing image url")
    }
}
