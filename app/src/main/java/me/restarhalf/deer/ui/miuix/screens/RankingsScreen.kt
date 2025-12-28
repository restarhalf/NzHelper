package me.restarhalf.deer.ui.miuix.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.supabase.LeaderboardEntry
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository
import me.restarhalf.deer.data.supabase.SupabaseLeaderboardRepository
import me.restarhalf.deer.ui.miuix.BottomNavItem
import me.restarhalf.deer.ui.util.AvatarCircle
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun RankingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val session by SupabaseAuthRepository.session.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    var errorText by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }

    val pullToRefreshState = rememberPullToRefreshState()

    suspend fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        errorText = null
        try {
            entries = SupabaseLeaderboardRepository.fetchLeaderboard(limit = 50)
        } catch (e: SupabaseApiException) {
            errorText = e.message
        } catch (e: Exception) {
            errorText = e.message ?: "刷新失败"
        } finally {
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "排行榜"
            )
        }
    ) { paddingValues ->
        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = { scope.launch { refresh() } },
            pullToRefreshState = pullToRefreshState,
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                overscrollEffect = null
            ) {
                item {
                    val title = if (session == null) "未登录" else "上传我的成绩"
                    val summary = when {
                        isSubmitting -> "处理中..."
                        session == null -> "前往设置登录后同步本地统计"
                        else -> "同步本地统计到排行榜"
                    }
                    Card {
                        BasicComponent(
                            title = title,
                            summary = summary,
                            enabled = !isSubmitting,
                            onClick = {
                                if (isSubmitting) return@BasicComponent
                                if (session == null) {
                                    navController.navigate(BottomNavItem.Settings.route)
                                    return@BasicComponent
                                }
                                isSubmitting = true
                                errorText = null
                                scope.launch {
                                    try {
                                        SupabaseLeaderboardRepository.upsertMyStatsFromLocal(context)
                                        refresh()
                                    } catch (e: SupabaseApiException) {
                                        errorText = e.message
                                    } catch (e: Exception) {
                                        errorText = e.message ?: "上传失败"
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        )
                    }
                }

                if (errorText != null) {
                    item {
                        Text(
                            text = errorText ?: "",
                            color = MiuixTheme.colorScheme.error
                        )
                    }
                }

                if (entries.isEmpty() && !isRefreshing && errorText == null) {
                    item {
                        Text(
                            text = "暂无数据",
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    }
                }

                itemsIndexed(entries) { index, entry ->
                    val rank = index + 1
                    val name = entry.nickname?.takeIf { it.isNotBlank() }
                        ?: entry.email?.takeIf { it.isNotBlank() }
                        ?: entry.userId?.take(8)
                        ?: "unknown"
                    val count = entry.totalCount ?: 0
                    val duration = formatDurationSeconds(entry.totalSeconds)
                    val avg = entry.avgMinutes?.let { String.format("%.1f", it) } ?: "0.0"

                    val avatarText = name
                        .trim()
                        .takeIf { it.isNotBlank() }
                        ?.first()
                        ?.uppercase()
                        ?: "?"

                    Card {
                        BasicComponent(
                            leftAction = {
                                AvatarCircle(
                                    avatarUrl = entry.avatarUrl,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(40.dp),
                                    containerColor = MiuixTheme.colorScheme.secondaryContainer,
                                    contentDescription = "头像"
                                ) {
                                    Text(
                                        text = avatarText,
                                        color = MiuixTheme.colorScheme.onSecondaryContainer,
                                        style = MiuixTheme.textStyles.title2
                                    )
                                }
                            },
                            title = "$rank. $name",
                            summary = "次数 $count · 总时长 $duration · 平均 $avg 分钟"
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun formatDurationSeconds(totalSeconds: Int?): String {
    val seconds = (totalSeconds ?: 0).coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) {
        "${hours}小时${minutes}分钟"
    } else {
        "${minutes}分钟"
    }
}

@Preview(showBackground = true)
@Composable
fun RankingsScreenPreview() {
    RankingsScreen(
        navController = NavController(LocalContext.current)
    )
}