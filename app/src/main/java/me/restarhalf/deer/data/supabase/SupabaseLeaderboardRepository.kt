package me.restarhalf.deer.data.supabase

import android.content.Context
import com.squareup.moshi.Types
import me.restarhalf.deer.data.SessionRepository
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant

object SupabaseLeaderboardRepository {
    private const val TABLE = "rankings"

    private val listType =
        Types.newParameterizedType(List::class.java, LeaderboardEntry::class.java)
    private val listAdapter = SupabaseHttp.moshi.adapter<List<LeaderboardEntry>>(listType)

    private val upsertListType =
        Types.newParameterizedType(List::class.java, UpsertLeaderboardRequest::class.java)
    private val upsertAdapter =
        SupabaseHttp.moshi.adapter<List<UpsertLeaderboardRequest>>(upsertListType)

    suspend fun fetchLeaderboard(limit: Int = 50): List<LeaderboardEntry> {
        val url = ("${SupabaseConfig.restBaseUrl}/$TABLE").toHttpUrl().newBuilder()
            .addQueryParameter("select", "*")
            .addQueryParameter("order", "total_seconds.desc.nullslast")
            .addQueryParameter("limit", limit.toString())
            .build()

        val request = SupabaseHttp.baseRequestBuilder(url)
            .get()
            .build()

        val body = SupabaseHttp.execute(request)
        return listAdapter.fromJson(body).orEmpty()
    }

    suspend fun upsertMyStatsFromLocal(context: Context): List<LeaderboardEntry> {
        val sessions = SessionRepository.loadSessions(context)
        val totalCount = sessions.size
        val totalSeconds = sessions.sumOf { it.duration }
        val avgMinutes =
            if (totalCount == 0) 0.0 else (totalSeconds.toDouble() / 60.0 / totalCount.toDouble())

        return upsertMyStats(
            context = context,
            totalCount = totalCount,
            totalSeconds = totalSeconds,
            avgMinutes = avgMinutes
        )
    }

    suspend fun upsertMyStats(
        context: Context,
        totalCount: Int,
        totalSeconds: Int,
        avgMinutes: Double
    ): List<LeaderboardEntry> {
        val session = SupabaseAuthRepository.session.value
            ?: SupabaseSessionRepository.load(context)
            ?: throw IllegalStateException("Not logged in")

        val accessToken = SupabaseAuthRepository.getValidAccessToken(context)
            ?: throw IllegalStateException("Not logged in")

        val url = ("${SupabaseConfig.restBaseUrl}/$TABLE").toHttpUrl().newBuilder()
            .addQueryParameter("on_conflict", "user_id")
            .build()

        val payload = listOf(
            UpsertLeaderboardRequest(
                userId = session.userId,
                nickname = session.nickname,
                email = session.email,
                avatarUrl = session.avatarUrl,
                totalCount = totalCount,
                totalSeconds = totalSeconds,
                avgMinutes = avgMinutes,
                updatedAt = Instant.now().toString()
            )
        )

        val json = upsertAdapter.toJson(payload)

        val request = SupabaseHttp.baseRequestBuilder(url, accessToken)
            .header("Prefer", "resolution=merge-duplicates,return=representation")
            .post(json.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        val body = SupabaseHttp.execute(request)
        return listAdapter.fromJson(body).orEmpty()
    }
}
