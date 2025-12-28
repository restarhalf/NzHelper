package me.restarhalf.deer.ui.miuix.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.Session
import me.restarhalf.deer.data.SessionRepository
import me.restarhalf.deer.ui.miuix.details.DetailsDialog
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Delete
import top.yukonga.miuix.kmp.icon.icons.useful.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sessions_prefs", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE_TIME }
    val sessions = remember { mutableStateListOf<Session>() }
    val scope = rememberCoroutineScope()

    var editSession by remember { mutableStateOf<Session?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    var remarkInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var watchedMovie by remember { mutableStateOf(false) }
    var climax by remember { mutableStateOf(false) }
    var rating by remember { mutableFloatStateOf(3f) }
    var mood by remember { mutableStateOf("平静") }
    var props by remember { mutableStateOf("手") }

    val showMenu = remember { mutableStateOf(false) }
    val showClearDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showViewDialog = remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<Session?>(null) }
    var sessionToView by remember { mutableStateOf<Session?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { os ->
                OutputStreamWriter(os).use { writer ->
                    val outList = sessions.map { s ->
                        listOf(
                            s.timestamp.format(formatter),
                            s.duration,
                            s.remark,
                            s.location,
                            s.watchedMovie,
                            s.climax,
                            s.rating,
                            s.mood,
                            s.props
                        )
                    }
                    writer.write(gson.toJson(outList))
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)
                ?.bufferedReader()
                ?.use { reader ->
                    val jsonStr = reader.readText()
                    val root = JsonParser.parseString(jsonStr).asJsonArray

                    sessions.clear()

                    for (elem in root) {
                        if (elem.isJsonArray) {
                            val arr = elem.asJsonArray
                            val timeStr = arr[0].asString
                            val dur = if (arr.size() >= 2) arr[1].asInt else 0
                            val rem =
                                if (arr.size() >= 3 && !arr[2].isJsonNull) arr[2].asString else ""
                            val loc =
                                if (arr.size() >= 4 && !arr[3].isJsonNull) arr[3].asString else ""
                            val watched = if (arr.size() >= 5) arr[4].asBoolean else false
                            val climaxed = if (arr.size() >= 6) arr[5].asBoolean else false
                            val rate = if (arr.size() >= 7 && !arr[6].isJsonNull) {
                                arr[6].asFloat.coerceIn(0f, 5f)
                            } else 0f
                            val md =
                                if (arr.size() >= 8 && !arr[7].isJsonNull) arr[7].asString else ""
                            val prop =
                                if (arr.size() >= 9 && !arr[8].isJsonNull) arr[8].asString else ""

                            sessions.add(
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

                    prefs.edit {
                        putString("sessions", jsonStr)
                    }
                }
        }
    }


    LaunchedEffect(Unit) {
        val loaded = SessionRepository.loadSessions(context)
        sessions.clear()
        sessions.addAll(loaded)
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "历史记录",
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu.value = true },
                            holdDownState = showMenu.value
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Useful.More,
                                contentDescription = "更多",
                                tint = MiuixTheme.colorScheme.onBackground
                            )
                        }

                        ListPopup(
                            show = showMenu,
                            alignment = PopupPositionProvider.Align.BottomRight,
                            onDismissRequest = { showMenu.value = false }
                        ) {
                            val menuItems = listOf("导出数据", "导入数据", "清除全部记录")
                            ListPopupColumn {
                                menuItems.forEachIndexed { index, text ->
                                    DropdownImpl(
                                        text = text,
                                        optionSize = menuItems.size,
                                        isSelected = false,
                                        onSelectedIndexChange = { selectedIdx ->
                                            showMenu.value = false
                                            when (selectedIdx) {
                                                0 -> exportLauncher.launch("NzHelper_export.json")
                                                1 -> importLauncher.launch(arrayOf("application/json"))
                                                2 -> showClearDialog.value = true
                                            }
                                        },
                                        index = index
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "(。・ω・。)",
                            style = MiuixTheme.textStyles.title2
                        )
                        Text(
                            text = "暂无历史记录哦！",
                            style = MiuixTheme.textStyles.body2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    overscrollEffect = null
                ) {
                    items(sessions) { session ->
                        Card {
                            BasicComponent(
                                title = "时间: " + session.timestamp.format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                ),
                                summary = buildString {
                                    append("持续: ")
                                    append(formatTime(session.duration))
                                    if (session.remark.isNotEmpty()) {
                                        append("\n备注: ")
                                        append(session.remark)
                                    }
                                },
                                onClick = {
                                    sessionToView = session
                                    showViewDialog.value = true
                                },
                                rightActions = {
                                    IconButton(
                                        onClick = {
                                            sessionToDelete = session
                                            showDeleteDialog.value = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Useful.Delete,
                                            contentDescription = "删除",
                                            tint = MiuixTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            SuperDialog(
                title = "删除记录",
                summary = "确认删除此记录？",
                show = showDeleteDialog,
                onDismissRequest = {
                    showDeleteDialog.value = false
                    sessionToDelete = null
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        text = "取消",
                        onClick = {
                            showDeleteDialog.value = false
                            sessionToDelete = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        text = "确认",
                        onClick = {
                            val target = sessionToDelete
                            if (target != null) {
                                sessions.remove(target)
                                scope.launch { SessionRepository.saveSessions(context, sessions) }
                            }
                            showDeleteDialog.value = false
                            sessionToDelete = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }

            SuperDialog(
                title = "清除全部记录",
                summary = "确认要清除所有历史记录吗？此操作不可撤销。",
                show = showClearDialog,
                onDismissRequest = { showClearDialog.value = false }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        text = "取消",
                        onClick = { showClearDialog.value = false },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        text = "删除",
                        onClick = {
                            sessions.clear()
                            prefs.edit { remove("sessions") }
                            showClearDialog.value = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }

            sessionToView?.let { s ->
                SuperDialog(
                    title = "会话详情",
                    show = showViewDialog,
                    onDismissRequest = {
                        showViewDialog.value = false
                        sessionToView = null
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        Text(
                            "开始时间：" + s.timestamp.format(pat),
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "持续时长：" + formatTime(s.duration),
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "备注：" + s.remark.ifEmpty { "无" },
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "地点：" + s.location.ifEmpty { "无" },
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "是否观看小电影：" + if (s.watchedMovie) "是" else "否",
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "发射：" + if (s.climax) "是" else "否",
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "道具：" + s.props.ifEmpty { "无" },
                            style = MiuixTheme.textStyles.body1
                        )
                        Text(
                            "评分：" + "%.1f".format(s.rating) + " / 5.0",
                            style = MiuixTheme.textStyles.body1
                        )
                        Text("心情：" + s.mood.ifEmpty { "无" }, style = MiuixTheme.textStyles.body1)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                text = "关闭",
                                onClick = {
                                    showViewDialog.value = false
                                    sessionToView = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            TextButton(
                                text = "编辑",
                                onClick = {
                                    editSession = s
                                    isEditing = true
                                    remarkInput = s.remark
                                    locationInput = s.location
                                    watchedMovie = s.watchedMovie
                                    climax = s.climax
                                    rating = s.rating
                                    mood = s.mood
                                    props = s.props
                                    showDetailsDialog = true
                                    showViewDialog.value = false
                                    sessionToView = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary()
                            )
                        }
                    }
                }
            }

            DetailsDialog(
                show = showDetailsDialog,
                remark = remarkInput,
                onRemarkChange = { remarkInput = it },
                location = locationInput,
                onLocationChange = { locationInput = it },
                watchedMovie = watchedMovie,
                onWatchedMovieChange = { watchedMovie = it },
                climax = climax,
                onClimaxChange = { climax = it },
                props = props,
                onPropsChange = { props = it },
                rating = rating,
                onRatingChange = { rating = it },
                mood = mood,
                onMoodChange = { mood = it },
                onConfirm = {
                    if (isEditing && editSession != null) {
                        val idx = sessions.indexOf(editSession!!)
                        if (idx >= 0) {
                            sessions[idx] = editSession!!.copy(
                                remark = remarkInput,
                                location = locationInput,
                                watchedMovie = watchedMovie,
                                climax = climax,
                                rating = rating,
                                mood = mood,
                                props = props
                            )
                        }
                    }

                    scope.launch {
                        SessionRepository.saveSessions(context, sessions)
                    }

                    remarkInput = ""
                    locationInput = ""
                    watchedMovie = false
                    climax = false
                    rating = 3f
                    mood = "平静"
                    props = "手"
                    showDetailsDialog = false
                    isEditing = false
                    editSession = null
                },
                onDismiss = {
                    showDetailsDialog = false
                    isEditing = false
                    editSession = null
                }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return buildString {
        if (hours > 0) append(String.format("%02d:", hours))
        append(String.format("%02d:%02d", minutes, seconds))
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HistoryScreen()
}