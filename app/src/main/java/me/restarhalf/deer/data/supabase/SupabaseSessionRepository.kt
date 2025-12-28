package me.restarhalf.deer.data.supabase

import android.content.Context
import androidx.core.content.edit

object SupabaseSessionRepository {
    private const val PREFS_NAME = "supabase_auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_EXPIRES_AT = "expires_at"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_NICKNAME="nickname"
    private const val KEY_AVATAR_URL = "avatar_url"

    fun load(context: Context): SupabaseSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null)
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)?.takeIf { it.isNotBlank() }
        val nickname = prefs.getString(KEY_NICKNAME, null)
            ?.takeIf { it.isNotBlank() }
            ?: email?.takeIf { it.isNotBlank() }
            ?: userId.take(8)

        if (expiresAt <= 0L) return null

        return SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            userId = userId,
            email = email,
            nickname = nickname,
            avatarUrl = avatarUrl
        )
    }

    fun save(context: Context, session: SupabaseSession) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, session.accessToken)
            putString(KEY_REFRESH_TOKEN, session.refreshToken)
            putLong(KEY_EXPIRES_AT, session.expiresAtEpochSeconds)
            putString(KEY_USER_ID, session.userId)
            putString(KEY_EMAIL, session.email)
            putString(KEY_NICKNAME, session.nickname)
            putString(KEY_AVATAR_URL, session.avatarUrl)
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_EXPIRES_AT)
            remove(KEY_USER_ID)
            remove(KEY_EMAIL)
            remove(KEY_NICKNAME)
            remove(KEY_AVATAR_URL)
        }
    }
}
