package me.restarhalf.deer.ui.md3.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.supabase.LeaderboardEntry
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository
import me.restarhalf.deer.data.supabase.SupabaseLeaderboardRepository
import me.restarhalf.deer.ui.util.AvatarCircle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RankingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val session by SupabaseAuthRepository.session.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }

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
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(text = "排名") },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val title = if (session == null) "未登录" else "上传我的成绩"
                val summary = when {
                    isSubmitting -> "处理中..."
                    session == null -> "请先登录后再上传"
                    else -> "同步本地统计到排行榜"
                }

                ElevatedCard {
                    androidx.compose.material3.ListItem(
                        headlineContent = { Text(text = title) },
                        supportingContent = { Text(text = summary) },
                        trailingContent = {
                            Row {
                                TextButton(
                                    onClick = { scope.launch { refresh() } },
                                    enabled = !isRefreshing
                                ) {
                                    Text("刷新")
                                }
                                TextButton(
                                    onClick = {
                                        if (session == null || isSubmitting) return@TextButton
                                        isSubmitting = true
                                        errorText = null
                                        scope.launch {
                                            try {
                                                SupabaseLeaderboardRepository.upsertMyStatsFromLocal(
                                                    context
                                                )
                                                refresh()
                                            } catch (e: SupabaseApiException) {
                                                errorText = e.message
                                            } catch (e: Exception) {
                                                errorText = e.message ?: "上传失败"
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    },
                                    enabled = session != null && !isSubmitting
                                ) {
                                    Text("上传")
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
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (entries.isEmpty() && !isRefreshing && errorText == null) {
                item {
                    Text(
                        text = "暂无数据",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                ElevatedCard {
                    androidx.compose.material3.ListItem(
                        leadingContent = {
                            AvatarCircle(
                                avatarUrl = entry.avatarUrl,
                                modifier = Modifier.size(40.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentDescription = "头像"
                            ) {
                                Text(
                                    text = avatarText,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        headlineContent = { Text(text = "$rank. $name") },
                        supportingContent = {
                            Text(text = "次数 $count · 总时长 $duration · 平均 $avg 分钟")
                        }
                    )
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
