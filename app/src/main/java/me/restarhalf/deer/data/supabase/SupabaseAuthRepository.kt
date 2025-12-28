package me.restarhalf.deer.data.supabase

import android.content.Context
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody

object SupabaseAuthRepository {
    private val _session = MutableStateFlow<SupabaseSession?>(null)
    val session: StateFlow<SupabaseSession?> = _session.asStateFlow()

    private val authAdapter = SupabaseHttp.moshi.adapter(SupabaseAuthResponse::class.java)
    private val emailPasswordAdapter =
        SupabaseHttp.moshi.adapter(SupabaseEmailPasswordRequest::class.java)
    private val emailOtpAdapter = SupabaseHttp.moshi.adapter(SupabaseEmailOtpRequest::class.java)
    private val verifyOtpAdapter = SupabaseHttp.moshi.adapter(SupabaseVerifyOtpRequest::class.java)
    private val signUpAdapter = SupabaseHttp.moshi.adapter(SupabaseSignUpRequest::class.java)
    private val updateUserMapAdapter = SupabaseHttp.moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Any::class.java
        )
    )
    private val userAdapter = SupabaseHttp.moshi.adapter(SupabaseUser::class.java)
    private val refreshAdapter = SupabaseHttp.moshi.adapter(SupabaseRefreshTokenRequest::class.java)

    fun init(context: Context) {
        _session.value = SupabaseSessionRepository.load(context)
    }

    fun signOut(context: Context) {
        SupabaseSessionRepository.clear(context)
        _session.value = null
    }

    suspend fun sendEmailOtp(
        email: String,
        createUser: Boolean? = null
    ) {
        val cleaned = email.trim().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Email is blank")

        val url = ("${SupabaseConfig.authBaseUrl}/otp").toHttpUrl()
        val payload = emailOtpAdapter.toJson(
            SupabaseEmailOtpRequest(
                email = cleaned,
                createUser = createUser
            )
        )
        val request = SupabaseHttp.baseRequestBuilder(url)
            .post(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        SupabaseHttp.execute(request)
    }

    suspend fun verifyEmailOtp(
        context: Context,
        email: String,
        token: String,
        type: String = "email"
    ): SupabaseSession {
        val cleanedEmail = email.trim().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Email is blank")
        val cleanedToken = token.trim().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Token is blank")

        val url = ("${SupabaseConfig.authBaseUrl}/verify").toHttpUrl()
        val payload = verifyOtpAdapter.toJson(
            SupabaseVerifyOtpRequest(
                type = type,
                email = cleanedEmail,
                token = cleanedToken
            )
        )
        val request = SupabaseHttp.baseRequestBuilder(url)
            .post(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        val body = SupabaseHttp.execute(request)
        val resp = authAdapter.fromJson(body)
            ?: throw SupabaseApiException(500, body, "Invalid auth response")

        val accessToken = resp.accessToken
        val refreshToken = resp.refreshToken
        val user = resp.user

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || user == null || user.id.isBlank()) {
            throw SupabaseApiException(500, body, "Missing token in auth response")
        }

        val now = System.currentTimeMillis() / 1000
        val expiresAt = now + (resp.expiresIn ?: 3600)

        val nickname = user.userMetadata?.nickname
            ?.takeIf { it.isNotBlank() }
            ?: user.email?.takeIf { it.isNotBlank() }
            ?: user.id.take(8)

        val avatarUrl = user.userMetadata?.avatarUrl
            ?.takeIf { it.isNotBlank() }

        val session = SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            userId = user.id,
            nickname = nickname,
            email = user.email,
            avatarUrl = avatarUrl
        )

        SupabaseSessionRepository.save(context, session)
        _session.value = session
        return session
    }

    suspend fun requestEmailChange(
        context: Context,
        newEmail: String
    ) {
        val cleaned = newEmail.trim().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Email is blank")

        val accessToken = getValidAccessToken(context)
            ?: throw IllegalStateException("Not logged in")

        val url = ("${SupabaseConfig.authBaseUrl}/user").toHttpUrl()
        val payload = updateUserMapAdapter.toJson(mapOf("email" to cleaned))
        val request = SupabaseHttp.baseRequestBuilder(url, accessToken)
            .put(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        SupabaseHttp.execute(request)
    }

    suspend fun sendPasswordRecoveryEmail(email: String) {
        val cleaned = email.trim().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Email is blank")

        val url = ("${SupabaseConfig.authBaseUrl}/recover").toHttpUrl()
        val payload = updateUserMapAdapter.toJson(mapOf("email" to cleaned))
        val request = SupabaseHttp.baseRequestBuilder(url)
            .post(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        SupabaseHttp.execute(request)
    }

    suspend fun resendSignupEmail(email: String) {
        val cleaned = email.trim().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Email is blank")

        val url = ("${SupabaseConfig.authBaseUrl}/resend").toHttpUrl()
        val payload = updateUserMapAdapter.toJson(
            mapOf(
                "type" to "signup",
                "email" to cleaned
            )
        )
        val request = SupabaseHttp.baseRequestBuilder(url)
            .post(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        SupabaseHttp.execute(request)
    }

    suspend fun resetPasswordWithRecoveryOtp(
        context: Context,
        email: String,
        token: String,
        newPassword: String
    ): SupabaseSession {
        val cleanedPassword = newPassword.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Password is blank")

        val verified = verifyEmailOtp(
            context = context,
            email = email,
            token = token,
            type = "recovery"
        )

        val url = ("${SupabaseConfig.authBaseUrl}/user").toHttpUrl()
        val payload = updateUserMapAdapter.toJson(mapOf("password" to cleanedPassword))
        val request = SupabaseHttp.baseRequestBuilder(url, verified.accessToken)
            .put(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        val body = SupabaseHttp.execute(request)
        val user = userAdapter.fromJson(body)
            ?: throw SupabaseApiException(500, body, "Invalid user response")

        val resolvedNickname = user.userMetadata?.nickname
            ?.takeIf { it.isNotBlank() }
            ?: verified.nickname

        val resolvedAvatarUrl = user.userMetadata?.avatarUrl
            ?.takeIf { it.isNotBlank() }
            ?: verified.avatarUrl

        val updated = verified.copy(
            userId = user.id,
            email = user.email ?: verified.email,
            nickname = resolvedNickname,
            avatarUrl = resolvedAvatarUrl
        )

        SupabaseSessionRepository.save(context, updated)
        _session.value = updated
        return updated
    }

    suspend fun signInWithPassword(
        context: Context,
        email: String,
        password: String
    ): SupabaseSession {
        val url = ("${SupabaseConfig.authBaseUrl}/token?grant_type=password").toHttpUrl()
        val payload = emailPasswordAdapter.toJson(
            SupabaseEmailPasswordRequest(
                email = email,
                password = password
            )
        )
        val request = SupabaseHttp.baseRequestBuilder(url)
            .post(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        val body = SupabaseHttp.execute(request)
        val resp = authAdapter.fromJson(body)
            ?: throw SupabaseApiException(500, body, "Invalid auth response")

        val accessToken = resp.accessToken
        val refreshToken = resp.refreshToken
        val user = resp.user

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || user == null || user.id.isBlank()) {
            throw SupabaseApiException(500, body, "Missing token in auth response")
        }

        val now = System.currentTimeMillis() / 1000
        val expiresAt = now + (resp.expiresIn ?: 3600)

        val nickname = user.userMetadata?.nickname
            ?.takeIf { it.isNotBlank() }
            ?: user.email?.takeIf { it.isNotBlank() }
            ?: user.id.take(8)

        val avatarUrl = user.userMetadata?.avatarUrl
            ?.takeIf { it.isNotBlank() }

        val session = SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            userId = user.id,
            nickname = nickname,
            email = user.email,
            avatarUrl = avatarUrl
        )

        SupabaseSessionRepository.save(context, session)
        _session.value = session
        return session
    }

    suspend fun signUpWithEmail(
        context: Context,
        nickname: String,
        email: String,
        password: String
    ): SupabaseSession? {
        val url = ("${SupabaseConfig.authBaseUrl}/signup").toHttpUrl()
        val payload = signUpAdapter.toJson(
            SupabaseSignUpRequest(
                email = email,
                password = password,
                data = SupabaseUserMetadata(
                    nickname = nickname.trim().takeIf { it.isNotBlank() }
                )
            )
        )
        val request = SupabaseHttp.baseRequestBuilder(url)
            .post(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        val body = SupabaseHttp.execute(request)
        val resp = authAdapter.fromJson(body)
            ?: throw SupabaseApiException(500, body, "Invalid auth response")

        val accessToken = resp.accessToken
        val refreshToken = resp.refreshToken
        val user = resp.user

        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank() || user == null || user.id.isBlank()) {
            return null
        }

        val now = System.currentTimeMillis() / 1000
        val expiresAt = now + (resp.expiresIn ?: 3600)

        val resolvedNickname = user.userMetadata?.nickname
            ?.takeIf { it.isNotBlank() }
            ?: nickname.trim().takeIf { it.isNotBlank() }
            ?: user.email?.takeIf { it.isNotBlank() }
            ?: user.id.take(8)

        val avatarUrl = user.userMetadata?.avatarUrl
            ?.takeIf { it.isNotBlank() }

        val session = SupabaseSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            userId = user.id,
            nickname = resolvedNickname,
            email = user.email,
            avatarUrl = avatarUrl
        )

        SupabaseSessionRepository.save(context, session)
        _session.value = session
        return session
    }

    suspend fun getValidAccessToken(context: Context): String? {
        val cur =
            _session.value ?: SupabaseSessionRepository.load(context)?.also { _session.value = it }
        if (cur == null) return null

        val now = System.currentTimeMillis() / 1000
        if (now < cur.expiresAtEpochSeconds - 30) {
            return cur.accessToken
        }

        val refreshed = refreshSession(context, cur)
        return refreshed?.accessToken
    }

    suspend fun updateMyProfile(
        context: Context,
        nickname: String? = null,
        avatarUrl: String? = null
    ): SupabaseSession {
        val initial =
            _session.value ?: SupabaseSessionRepository.load(context)?.also { _session.value = it }
            ?: throw IllegalStateException("Not logged in")

        val accessToken = getValidAccessToken(context)
            ?: throw IllegalStateException("Not logged in")

        val cur = _session.value ?: initial

        val data = mutableMapOf<String, Any?>()
        val nicknameValue = nickname?.trim()?.takeIf { it.isNotBlank() }
        val avatarValue = avatarUrl?.trim()?.takeIf { it.isNotBlank() }
        if (nicknameValue != null) data["nickname"] = nicknameValue
        if (avatarValue != null) data["avatar_url"] = avatarValue

        if (data.isEmpty()) return cur

        val url = ("${SupabaseConfig.authBaseUrl}/user").toHttpUrl()
        val payload = updateUserMapAdapter.toJson(mapOf("data" to data))

        val request = SupabaseHttp.baseRequestBuilder(url, accessToken)
            .put(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        val body = SupabaseHttp.execute(request)
        val user = userAdapter.fromJson(body)
            ?: throw SupabaseApiException(500, body, "Invalid user response")

        val resolvedNickname = user.userMetadata?.nickname
            ?.takeIf { it.isNotBlank() }
            ?: cur.nickname

        val resolvedAvatarUrl = user.userMetadata?.avatarUrl
            ?.takeIf { it.isNotBlank() }
            ?: cur.avatarUrl

        val session = cur.copy(
            userId = user.id,
            email = user.email ?: cur.email,
            nickname = resolvedNickname,
            avatarUrl = resolvedAvatarUrl
        )

        SupabaseSessionRepository.save(context, session)
        _session.value = session
        return session
    }

    suspend fun updateMyNickname(context: Context, nickname: String): SupabaseSession {
        return updateMyProfile(
            context = context,
            nickname = nickname
        )
    }

    private suspend fun refreshSession(context: Context, cur: SupabaseSession): SupabaseSession? {
        val url = ("${SupabaseConfig.authBaseUrl}/token?grant_type=refresh_token").toHttpUrl()
        val payload =
            refreshAdapter.toJson(SupabaseRefreshTokenRequest(refreshToken = cur.refreshToken))
        val request = SupabaseHttp.baseRequestBuilder(url)
            .post(payload.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        val body = SupabaseHttp.execute(request)
        val resp = authAdapter.fromJson(body)
            ?: throw SupabaseApiException(500, body, "Invalid refresh response")

        val accessToken = resp.accessToken
        val newRefreshToken = resp.refreshToken

        if (accessToken.isNullOrBlank() || newRefreshToken.isNullOrBlank()) {
            signOut(context)
            return null
        }

        val now = System.currentTimeMillis() / 1000
        val expiresAt = now + (resp.expiresIn ?: 3600)

        val userId = resp.user?.id?.takeIf { it.isNotBlank() } ?: cur.userId
        val email = resp.user?.email ?: cur.email
        val nickname = resp.user?.userMetadata?.nickname
            ?.takeIf { it.isNotBlank() }
            ?: cur.nickname

        val avatarUrl = resp.user?.userMetadata?.avatarUrl
            ?.takeIf { it.isNotBlank() }
            ?: cur.avatarUrl

        val session = SupabaseSession(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            expiresAtEpochSeconds = expiresAt,
            userId = userId,
            nickname = nickname,
            email = email,
            avatarUrl = avatarUrl
        )

        SupabaseSessionRepository.save(context, session)
        _session.value = session
        return session
    }
}
