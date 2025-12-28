package me.restarhalf.deer.data.supabase

import com.squareup.moshi.Json

data class SupabaseUserMetadata(
    @param:Json(name = "nickname") val nickname: String?,
    @param:Json(name = "avatar_url") val avatarUrl: String? = null
)

data class SupabaseUser(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "email") val email: String?,
    @param:Json(name = "user_metadata") val userMetadata: SupabaseUserMetadata?
)

data class SupabaseAuthResponse(
    @param:Json(name = "access_token") val accessToken: String?,
    @param:Json(name = "token_type") val tokenType: String?,
    @param:Json(name = "expires_in") val expiresIn: Long?,
    @param:Json(name = "refresh_token") val refreshToken: String?,
    @param:Json(name = "user") val user: SupabaseUser?
)

data class SupabaseErrorResponse(
    @param:Json(name = "msg") val msg: String?,
    @param:Json(name = "message") val message: String?,
    @param:Json(name = "error") val error: String?,
    @param:Json(name = "error_description") val errorDescription: String?
)

data class SupabaseEmailPasswordRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String
)

data class SupabaseSignUpRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "data") val data: SupabaseUserMetadata?
)

data class SupabaseUpdateUserRequest(
    @param:Json(name = "data") val data: SupabaseUserMetadata?
)

data class SupabaseRefreshTokenRequest(
    @param:Json(name = "refresh_token") val refreshToken: String
)

data class SupabaseEmailOtpRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "create_user") val createUser: Boolean? = null
)

data class SupabaseVerifyOtpRequest(
    @param:Json(name = "type") val type: String,
    @param:Json(name = "email") val email: String,
    @param:Json(name = "token") val token: String
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
    @param:Json(name = "user_id") val userId: String?,
    @param:Json(name = "nickname") val nickname: String?,
    @param:Json(name = "email") val email: String?,
    @param:Json(name = "avatar_url") val avatarUrl: String?,
    @param:Json(name = "total_count") val totalCount: Int?,
    @param:Json(name = "total_seconds") val totalSeconds: Int?,
    @param:Json(name = "avg_minutes") val avgMinutes: Double?,
    @param:Json(name = "updated_at") val updatedAt: String?
)

data class UpsertLeaderboardRequest(
    @param:Json(name = "user_id") val userId: String,
    @param:Json(name = "nickname") val nickname: String?,
    @param:Json(name = "email") val email: String?,
    @param:Json(name = "avatar_url") val avatarUrl: String? = null,
    @param:Json(name = "total_count") val totalCount: Int,
    @param:Json(name = "total_seconds") val totalSeconds: Int,
    @param:Json(name = "avg_minutes") val avgMinutes: Double,
    @param:Json(name = "updated_at") val updatedAt: String
)