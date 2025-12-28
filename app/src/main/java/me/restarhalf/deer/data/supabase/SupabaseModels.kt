package me.restarhalf.deer.data.supabase

import com.squareup.moshi.Json

data class SupabaseUserMetadata(
    @Json(name = "nickname") val nickname: String?,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

data class SupabaseUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String?,
    @Json(name = "user_metadata") val userMetadata: SupabaseUserMetadata?
)

data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "token_type") val tokenType: String?,
    @Json(name = "expires_in") val expiresIn: Long?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "user") val user: SupabaseUser?
)

data class SupabaseErrorResponse(
    @Json(name = "msg") val msg: String?,
    @Json(name = "message") val message: String?,
    @Json(name = "error") val error: String?,
    @Json(name = "error_description") val errorDescription: String?
)

data class SupabaseEmailPasswordRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

data class SupabaseSignUpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "data") val data: SupabaseUserMetadata?
)

data class SupabaseUpdateUserRequest(
    @Json(name = "data") val data: SupabaseUserMetadata?
)

data class SupabaseRefreshTokenRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
    val userId: String,
    val nickname: String,
    val email: String?,
    val avatarUrl: String? = null
)

data class LeaderboardEntry(
    @Json(name = "user_id") val userId: String?,
    @Json(name = "nickname") val nickname: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "total_count") val totalCount: Int?,
    @Json(name = "total_seconds") val totalSeconds: Int?,
    @Json(name = "avg_minutes") val avgMinutes: Double?,
    @Json(name = "updated_at") val updatedAt: String?
)

data class UpsertLeaderboardRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "nickname") val nickname: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "total_seconds") val totalSeconds: Int,
    @Json(name = "avg_minutes") val avgMinutes: Double,
    @Json(name = "updated_at") val updatedAt: String
)