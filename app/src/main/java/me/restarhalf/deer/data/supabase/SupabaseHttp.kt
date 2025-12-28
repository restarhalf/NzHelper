package me.restarhalf.deer.data.supabase

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request

object SupabaseHttp {
    private val client = OkHttpClient()

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(SupabaseErrorResponse::class.java)

    val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun baseRequestBuilder(url: HttpUrl, accessToken: String? = null): Request.Builder {
        val token = if (!accessToken.isNullOrBlank()) accessToken else SupabaseConfig.anonKey
        val builder = Request.Builder()
            .url(url)
            .header("apikey", SupabaseConfig.anonKey)
            .header("Authorization", "Bearer $token")

        return builder
    }

    suspend fun execute(request: Request): String = withContext(Dispatchers.IO) {
        val resp = client.newCall(request).execute()
        val body = resp.body.string()

        if (!resp.isSuccessful) {
            val parsed = runCatching { body.let { errorAdapter.fromJson(it) } }.getOrNull()
            val msg = parsed?.msg
                ?: parsed?.message
                ?: parsed?.errorDescription
                ?: parsed?.error
                ?: "HTTP ${resp.code}"
            throw SupabaseApiException(resp.code, body, msg)
        }

        body.orEmpty()
    }
}
