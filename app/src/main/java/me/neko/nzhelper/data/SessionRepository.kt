package me.neko.nzhelper.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 公共数据类
data class Session(
    val timestamp: LocalDateTime,
    val duration: Int,
    val remark: String,
    val location: String,
    val watchedMovie: Boolean,
    val climax: Boolean,
    val rating: Float,
    val mood: String,
    val props: String
)

// 序列化/反序列化和持久化
object SessionRepository {
    private const val PREFS_NAME = "sessions_prefs"
    private const val KEY_SESSIONS = "sessions"
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val gson = Gson()

    /**
     * 从 SharedPreferences 加载会话列表
     */
    suspend fun loadSessions(context: Context): List<Session> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SESSIONS, "[]") ?: "[]"
        val root = JsonParser.parseString(jsonStr).asJsonArray
        val list = mutableListOf<Session>()
        for (elem in root) {
            if (elem.isJsonArray) {
                val arr = elem.asJsonArray
                val timeStr = arr[0].asString
                val dur = if (arr.size() >= 2) arr[1].asInt else 0
                val rem = if (arr.size() >= 3 && !arr[2].isJsonNull) arr[2].asString else ""
                val loc = if (arr.size() >= 4 && !arr[3].isJsonNull) arr[3].asString else ""
                val watched = if (arr.size() >= 5) arr[4].asBoolean else false
                val climaxed = if (arr.size() >= 6) arr[5].asBoolean else false
                val rate = if (arr.size() >= 7 && !arr[6].isJsonNull) {
                    arr[6].asFloat.coerceIn(0f, 5f) // 确保在范围内
                } else 0f
                val md = if (arr.size() >= 8 && !arr[7].isJsonNull) arr[7].asString else ""
                val prop = if (arr.size() >= 9 && !arr[8].isJsonNull) arr[8].asString else ""
                list.add(
                    Session(
                        timestamp = LocalDateTime.parse(timeStr, formatter),
                        duration = dur,
                        remark = rem,
                        location = loc,
                        watchedMovie = watched,
                        climax = climaxed,
                        rating = rate,
                        mood = md,
                        props = prop
                    )
                )
            }
        }
        list
    }

    /**
     * 将会话列表保存到 SharedPreferences
     */
    suspend fun saveSessions(context: Context, sessions: List<Session>) =
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
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
                val jsonStr = gson.toJson(jsonArray)
                putString(KEY_SESSIONS, jsonStr)
            }
        }
}
