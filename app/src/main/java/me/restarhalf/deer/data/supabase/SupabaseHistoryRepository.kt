package me.restarhalf.deer.data.supabase

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.squareup.moshi.Json
import com.squareup.moshi.Types
import me.restarhalf.deer.data.Session
import me.restarhalf.deer.data.SessionRepository
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private data class SessionHistoryRow(
    @param:Json(name = "user_id") val userId: String?,
    @param:Json(name = "sessions_json") val sessionsJson: String?,
    @param:Json(name = "updated_at") val updatedAt: String?
)

private data class UpsertSessionHistoryRequest(
    @param:Json(name = "user_id") val userId: String,
    @param:Json(name = "sessions_json") val sessionsJson: String,
    @param:Json(name = "updated_at") val updatedAt: String
)

object SupabaseHistoryRepository {
    private const val TABLE = "session_histories"

    private val gson = Gson()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    enum class SmartSyncAction {
        MERGE_UPLOAD,
        MERGE_DOWNLOAD
    }

    data class SmartSyncResult(
        val sessions: List<Session>,
        val action: SmartSyncAction
    )

    private val listType = Types.newParameterizedType(List::class.java, SessionHistoryRow::class.java)
    private val listAdapter = SupabaseHttp.moshi.adapter<List<SessionHistoryRow>>(listType)

    private val upsertListType =
        Types.newParameterizedType(List::class.java, UpsertSessionHistoryRequest::class.java)
    private val upsertAdapter =
        SupabaseHttp.moshi.adapter<List<UpsertSessionHistoryRequest>>(upsertListType)

    suspend fun uploadMyLocalHistory(context: Context) {
        val sessions = SessionRepository.loadSessions(context)
        uploadSessions(context, sessions)
    }

    suspend fun uploadSessions(context: Context, sessions: List<Session>) {
        val session = requireSession(context)
        val accessToken = requireAccessToken(context)

        val url = ("${SupabaseConfig.restBaseUrl}/$TABLE").toHttpUrl().newBuilder()
            .addQueryParameter("on_conflict", "user_id")
            .build()

        val sessionsJson = sessionsToJson(sessions)
        val payload = listOf(
            UpsertSessionHistoryRequest(
                userId = session.userId,
                sessionsJson = sessionsJson,
                updatedAt = Instant.now().toString()
            )
        )

        val json = upsertAdapter.toJson(payload)

        val request = SupabaseHttp.baseRequestBuilder(url, accessToken)
            .header("Prefer", "resolution=merge-duplicates,return=representation")
            .post(json.toRequestBody(SupabaseHttp.jsonMediaType))
            .build()

        SupabaseHttp.execute(request)
    }

    suspend fun downloadAndOverwriteLocal(context: Context): List<Session> {
        val sessionsJson = downloadMyHistoryJson(context)
        val remoteSessions = sessionsFromJson(sessionsJson)
        SessionRepository.saveSessions(context, remoteSessions)
        return remoteSessions
    }

    suspend fun downloadMergeAndUpload(context: Context): List<Session> {
        val local = SessionRepository.loadSessions(context)
        val remote = sessionsFromJson(downloadMyHistoryJson(context))
        val merged = mergeSessions(local, remote)
        SessionRepository.saveSessions(context, merged)
        uploadSessions(context, merged)
        return merged
    }

    suspend fun smartSync(context: Context): SmartSyncResult {
        val local = SessionRepository.loadSessions(context)
        val remote = sessionsFromJson(downloadMyHistoryJson(context))
        val merged = mergeSessions(local, remote)
        SessionRepository.saveSessions(context, merged)

        val localMax = local.maxOfOrNull { it.timestamp }
        val remoteMax = remote.maxOfOrNull { it.timestamp }
        val localIsNewer = when {
            localMax != null && remoteMax == null -> true
            localMax == null && remoteMax != null -> false
            localMax == null && remoteMax == null -> false
            else -> localMax!!.isAfter(remoteMax!!)
        }

        if (localIsNewer) {
            uploadSessions(context, merged)
            return SmartSyncResult(merged, SmartSyncAction.MERGE_UPLOAD)
        }

        return SmartSyncResult(merged, SmartSyncAction.MERGE_DOWNLOAD)
    }

    private suspend fun downloadMyHistoryJson(context: Context): String {
        val session = requireSession(context)
        val accessToken = requireAccessToken(context)

        val url = ("${SupabaseConfig.restBaseUrl}/$TABLE").toHttpUrl().newBuilder()
            .addQueryParameter("select", "sessions_json,updated_at")
            .addQueryParameter("user_id", "eq.${session.userId}")
            .addQueryParameter("limit", "1")
            .build()

        val request = SupabaseHttp.baseRequestBuilder(url, accessToken)
            .get()
            .build()

        val body = SupabaseHttp.execute(request)
        val row = listAdapter.fromJson(body)?.firstOrNull()
        return row?.sessionsJson?.takeIf { it.isNotBlank() } ?: "[]"
    }

    private fun requireSession(context: Context): SupabaseSession {
        return SupabaseAuthRepository.session.value
            ?: SupabaseSessionRepository.load(context)
            ?: throw IllegalStateException("Not logged in")
    }

    private suspend fun requireAccessToken(context: Context): String {
        return SupabaseAuthRepository.getValidAccessToken(context)
            ?: throw IllegalStateException("Not logged in")
    }

    private fun sessionsToJson(sessions: List<Session>): String {
        val jsonArray = sessions.map { session ->
            listOf(
                session.timestamp.format(formatter),
                session.duration,
                session.remark,
                session.location,
                session.watchedMovie,
                session.climax,
                session.rating,
                session.mood,
                session.props
            )
        }
        return gson.toJson(jsonArray)
    }

    private fun sessionsFromJson(jsonStr: String): List<Session> {
        val root = runCatching { JsonParser.parseString(jsonStr).asJsonArray }
            .getOrElse { return emptyList() }
        val list = mutableListOf<Session>()
        for (elem in root) {
            if (elem.isJsonArray) {
                val arr = elem.asJsonArray
                if (arr.size() <= 0) continue
                val timeStr = arr[0].asString
                val dur = if (arr.size() >= 2) arr[1].asInt else 0
                val rem = if (arr.size() >= 3 && !arr[2].isJsonNull) arr[2].asString else ""
                val loc = if (arr.size() >= 4 && !arr[3].isJsonNull) arr[3].asString else ""
                val watched = if (arr.size() >= 5) arr[4].asBoolean else false
                val climaxed = if (arr.size() >= 6) arr[5].asBoolean else false
                val rate = if (arr.size() >= 7 && !arr[6].isJsonNull) {
                    arr[6].asFloat.coerceIn(0f, 5f)
                } else 0f
                val mood = if (arr.size() >= 8 && !arr[7].isJsonNull) arr[7].asString else ""
                val props = if (arr.size() >= 9 && !arr[8].isJsonNull) arr[8].asString else ""

                val timestamp = runCatching { LocalDateTime.parse(timeStr, formatter) }.getOrNull()
                    ?: continue

                list.add(
                    Session(
                        timestamp = timestamp,
                        duration = dur,
                        remark = rem,
                        location = loc,
                        watchedMovie = watched,
                        climax = climaxed,
                        rating = rate,
                        mood = mood,
                        props = props
                    )
                )
            }
        }
        return list
    }

    private fun mergeSessions(local: List<Session>, remote: List<Session>): List<Session> {
        val merged = LinkedHashMap<String, Session>()
        for (s in remote) {
            merged[s.timestamp.format(formatter)] = s
        }
        for (s in local) {
            merged[s.timestamp.format(formatter)] = s
        }
        return merged.values.sortedBy { it.timestamp }
    }
}
