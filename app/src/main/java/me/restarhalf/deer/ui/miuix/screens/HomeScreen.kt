package me.restarhalf.deer.ui.miuix.screens

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.Session
import me.restarhalf.deer.data.SessionRepository
import me.restarhalf.deer.ui.service.TimerService
import me.restarhalf.deer.ui.miuix.details.DetailsDialog
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.LocalDateTime

@Composable
fun HomeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val serviceIntent = remember { Intent(context, TimerService::class.java) }
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

    LaunchedEffect(Unit) {
        ContextCompat.startForegroundService(
            context,
            serviceIntent.apply { action = TimerService.ACTION_START }
        )
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }
    DisposableEffect(Unit) {
        onDispose { context.unbindService(connection) }
    }

    val elapsedSeconds by timerService
        ?.elapsedSec
        ?.collectAsState(initial = 0)
        ?: remember { mutableIntStateOf(0) }

    var isRunning by remember { mutableStateOf(false) }
    val showConfirmDialog = remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    var remarkInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var watchedMovie by remember { mutableStateOf(false) }
    var climax by remember { mutableStateOf(false) }
    var rating by remember { mutableFloatStateOf(3f) }
    var mood by remember { mutableStateOf("平静") }
    var props by remember { mutableStateOf("手") }

    val sessions = remember { mutableStateListOf<Session>() }
    LaunchedEffect(Unit) {
        val loaded = SessionRepository.loadSessions(context)
        sessions.clear()
        sessions.addAll(loaded)
    }

    LaunchedEffect(isRunning) {
        val action = if (isRunning) TimerService.ACTION_START else TimerService.ACTION_PAUSE
        context.startService(serviceIntent.apply { this.action = action })
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "牛子小助手"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "记录新的手艺活",
                    style = MiuixTheme.textStyles.title2
                )
                Text(
                    text = if (isRunning) "计时中" else "准备开始",
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Text(
                    text = formatTime(elapsedSeconds),
                    style = MiuixTheme.textStyles.title1,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isRunning = !isRunning },
                        minWidth = 64.dp,
                        minHeight = 64.dp,
                        cornerRadius = 24.dp,
                        backgroundColor = MiuixTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "暂停" else "开始",
                            tint = MiuixTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (elapsedSeconds > 0) showConfirmDialog.value = true
                            else Toast.makeText(context, "计时尚未开始", Toast.LENGTH_SHORT).show()
                        },
                        minWidth = 64.dp,
                        minHeight = 64.dp,
                        cornerRadius = 24.dp,
                        backgroundColor = MiuixTheme.colorScheme.errorContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "结束",
                            tint = MiuixTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            SuperDialog(
                title = "结束",
                summary = "要结束对牛牛的爱抚了吗？",
                show = showConfirmDialog,
                onDismissRequest = { showConfirmDialog.value = false }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        text = "再坚持一下",
                        onClick = { showConfirmDialog.value = false },
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                    TextButton(
                        text = "燃尽了",
                        onClick = {
                            showConfirmDialog.value = false
                            showDetailsDialog = true
                            isRunning = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
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
                    val now = LocalDateTime.now()
                    val session = Session(
                        timestamp = now,
                        duration = elapsedSeconds,
                        remark = remarkInput,
                        location = locationInput,
                        watchedMovie = watchedMovie,
                        climax = climax,
                        rating = rating,
                        mood = mood,
                        props = props
                    )
                    sessions.add(session)
                    scope.launch { SessionRepository.saveSessions(context, sessions) }

                    isRunning = false
                    remarkInput = ""
                    locationInput = ""
                    watchedMovie = false
                    climax = false
                    rating = 3f
                    mood = "平静"
                    props = "手"
                    showDetailsDialog = false

                    context.startService(
                        serviceIntent.apply { action = TimerService.ACTION_STOP }
                    )
                },
                onDismiss = { showDetailsDialog = false }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatTime(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return buildString {
        if (h > 0) append(String.format("%02d:", h))
        append(String.format("%02d:%02d", m, s))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}