package me.restarhalf.deer.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.Session
import me.restarhalf.deer.data.SessionDraft
import me.restarhalf.deer.data.SessionRepository
import me.restarhalf.deer.ui.components.TwoTextButtonsRow
import me.restarhalf.deer.ui.custom.icons.FilledPlay
import me.restarhalf.deer.ui.custom.icons.Stop
import me.restarhalf.deer.ui.dialogs.DetailsDialog
import me.restarhalf.deer.ui.service.TimerService
import me.restarhalf.deer.ui.util.formatTime
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val bindIntent = remember { Intent(context, TimerService::class.java) }
    var timerService by remember { mutableStateOf<TimerService?>(null) }
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                timerService = (binder as TimerService.LocalBinder).getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                timerService = null
            }
        }
    }

    DisposableEffect(Unit) {
        context.bindService(bindIntent, connection, Context.BIND_AUTO_CREATE)
        onDispose { runCatching { context.unbindService(connection) } }
    }

    val elapsedSeconds by timerService
        ?.elapsedSec
        ?.collectAsState(initial = 0)
        ?: remember { mutableIntStateOf(0) }
    var isTwo by remember { mutableStateOf(false) }
    val isRunning by timerService
        ?.isRunning
        ?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }
    val showConfirmDialog = remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    var draft by remember { mutableStateOf(SessionDraft()) }

    val sessions = remember { mutableStateListOf<Session>() }
    LaunchedEffect(Unit) {
        val loaded = SessionRepository.loadSessions(context)
        sessions.clear()
        sessions.addAll(loaded)
    }

    val todayCount by remember(sessions) {
        derivedStateOf {
            val today = LocalDate.now()
            sessions.count { it.timestamp.toLocalDate() == today }
        }
    }

    fun startTimer() {
        ContextCompat.startForegroundService(
            context,
            TimerService.startIntent(context)
        )
    }

    fun pauseTimer() {
        context.startService(TimerService.pauseIntent(context))
    }

    fun stopTimer() {
        context.startService(TimerService.stopIntent(context))
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "今天🦌了吗",
                color = Color.Transparent
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.surfaceContainer)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "今日战绩: $todayCount 次",
                    style = MiuixTheme.textStyles.body1,
                    color = colorScheme.onBackground.copy(0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isRunning) colorScheme.primary else colorScheme.outline,
                        shape = CircleShape,
                    )
                    .background(colorScheme.surfaceContainer)

            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .border(
                            width = 1.dp,
                            color = colorScheme.outline.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formatTime(elapsedSeconds),
                        style = MiuixTheme.textStyles.title1.copy(
                            fontSize = 56.sp,
                            fontFeatureSettings = "enum"
                        ),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        color = colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isRunning) colorScheme.primary.copy(alpha = 0.1f)
                                else colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isRunning) "进行中" else "已暂停",
                            style = MiuixTheme.textStyles.body2,
                            color = if (isRunning) colorScheme.primary else colorScheme.onSurface.copy(
                                alpha = 0.6f
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTwo) {
                    IconButton(
                        onClick = {
                            showConfirmDialog.value = true
                        },
                        minWidth = 64.dp,
                        minHeight = 64.dp,
                        cornerRadius = 32.dp,
                        backgroundColor = colorScheme.surfaceContainer
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Stop,
                            contentDescription = "结束",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(90.dp))

                    IconButton(
                        onClick = {
                            if (isRunning) {
                                pauseTimer()
                            } else {
                                startTimer()
                            }
                        },
                        minWidth = 64.dp,
                        minHeight = 64.dp,
                        cornerRadius = 32.dp,
                        backgroundColor = colorScheme.surfaceContainer
                    ) {
                        Icon(
                            imageVector = if (isRunning) MiuixIcons.Pause else MiuixIcons.FilledPlay,
                            contentDescription = if (isRunning) "暂停" else "继续",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            startTimer()
                            isTwo = true
                        },
                        minWidth = 160.dp,
                        minHeight = 64.dp,
                        cornerRadius = 32.dp,
                        backgroundColor = colorScheme.surfaceContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MiuixIcons.FilledPlay,
                                contentDescription = "开始",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        WindowDialog(
            title = "结束",
            summary = "要结束对牛牛的爱抚了吗？",
            show = showConfirmDialog,
            onDismissRequest = { showConfirmDialog.value = false }
        ) {
            TwoTextButtonsRow(
                leftText = "再坚持一下",
                onLeftClick = { showConfirmDialog.value = false },
                rightText = "燃尽了",
                onRightClick = {
                    showConfirmDialog.value = false
                    showDetailsDialog = true
                    pauseTimer()
                },
            )
        }

        DetailsDialog(
            show = showDetailsDialog,
            draft = draft,
            onDraftChange = { draft = it },
            onConfirm = {
                val now = LocalDateTime.now()
                val session = draft.toSession(timestamp = now, duration = elapsedSeconds)
                sessions.add(session)
                scope.launch { SessionRepository.saveSessions(context, sessions) }

                draft = SessionDraft()
                showDetailsDialog = false

                stopTimer()
                isTwo = false
            },
            onDismiss = { showDetailsDialog = false }
        )
    }
}
