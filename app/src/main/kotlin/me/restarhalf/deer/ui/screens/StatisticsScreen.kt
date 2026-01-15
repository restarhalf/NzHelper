package me.restarhalf.deer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.restarhalf.deer.data.Session
import me.restarhalf.deer.data.SessionRepository
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollHorizontal
import top.yukonga.miuix.kmp.utils.overScrollVertical
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun StatisticsScreen() {
    val context = LocalContext.current
    val sessions = remember { mutableStateListOf<Session>() }

    LaunchedEffect(Unit) {
        val loaded = SessionRepository.loadSessions(context)
        sessions.clear()
        sessions.addAll(loaded)
    }

    val currentTime = LocalDateTime.now()

    val weekStats by remember {
        derivedStateOf {
            calculatePeriodStats(sessions, PeriodType.WEEK, currentTime)
        }
    }
    val monthStats by remember {
        derivedStateOf {
            calculatePeriodStats(sessions, PeriodType.MONTH, currentTime)
        }
    }
    val yearStats by remember {
        derivedStateOf {
            calculatePeriodStats(sessions, PeriodType.YEAR, currentTime)
        }
    }

    val weekCount by remember {
        derivedStateOf {
            sessions.count {
                it.timestamp >= currentTime.minusDays(currentTime.dayOfWeek.value.toLong() - 1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
            }
        }
    }
    val monthCount by remember {
        derivedStateOf {
            sessions.count {
                it.timestamp >= currentTime.withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
            }
        }
    }
    val yearCount by remember {
        derivedStateOf {
            sessions.count {
                it.timestamp >= currentTime.withDayOfYear(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
            }
        }
    }

    val totalStats by remember {
        derivedStateOf {
            if (sessions.isEmpty()) {
                Triple(0, 0, 0f)
            } else {
                val totalCount = sessions.size
                val totalSeconds = sessions.sumOf { it.duration }
                val avgMinutes = totalSeconds.toFloat() / (60 * totalCount)
                Triple(totalCount, totalSeconds, avgMinutes)
            }
        }
    }

    val latestSessionInfo by remember {
        derivedStateOf {
            if (sessions.isEmpty()) {
                null
            } else {
                val latest = sessions.maxByOrNull { it.timestamp }!!
                val lastDate = latest.timestamp.toLocalDate()
                val daysAgo = ChronoUnit.DAYS.between(lastDate, LocalDateTime.now().toLocalDate())

                val displayDate = when (daysAgo) {
                    0L -> "今天"
                    1L -> "昨天"
                    else -> lastDate.format(DateTimeFormatter.ofPattern("M月d日"))
                }

                val time = latest.timestamp.format(
                    DateTimeFormatter.ofPattern("a h:mm").withLocale(Locale.CHINA)
                )

                val breakDetail = when (daysAgo) {
                    0L -> "今日已交作业"
                    1L -> "昨天有过记录"
                    else -> "已经鸽了 $daysAgo 天"
                }

                LatestSessionInfo(
                    daysAgo = daysAgo,
                    displayDate = displayDate,
                    time = time,
                    durationSeconds = latest.duration,
                    breakDetail = breakDetail
                )
            }
        }
    }

    val weekDailyStats by remember {
        derivedStateOf {
            if (sessions.isEmpty()) emptyList()
            else {
                val now = LocalDateTime.now()
                val monday = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                    .toLocalDate()

                val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

                val statsMap = sessions
                    .filter { it.timestamp.toLocalDate() >= monday }
                    .groupBy { it.timestamp.toLocalDate() }
                    .mapValues { entry ->
                        DailyStat(
                            count = entry.value.size,
                            totalDuration = entry.value.sumOf { it.duration }
                        )
                    }

                weekDays.map { date ->
                    val dayOfWeekName = when (date.dayOfWeek) {
                        DayOfWeek.MONDAY -> "一"
                        DayOfWeek.TUESDAY -> "二"
                        DayOfWeek.WEDNESDAY -> "三"
                        DayOfWeek.THURSDAY -> "四"
                        DayOfWeek.FRIDAY -> "五"
                        DayOfWeek.SATURDAY -> "六"
                        DayOfWeek.SUNDAY -> "日"
                    }
                    val stat = statsMap[date] ?: DailyStat(count = 0, totalDuration = 0)
                    dayOfWeekName to (stat.totalDuration / 60f)
                }
            }
        }
    }

    val monthDailyStats by remember {
        derivedStateOf {
            if (sessions.isEmpty()) emptyList()
            else {
                val now = LocalDateTime.now()
                val firstDayOfMonth = now.withDayOfMonth(1).toLocalDate()

                sessions
                    .filter {
                        val date = it.timestamp.toLocalDate()
                        date >= firstDayOfMonth
                    }
                    .groupBy { it.timestamp.toLocalDate() }
                    .mapValues { entry ->
                        entry.value.sumOf { it.duration } / 60f
                    }
                    .filter { it.value > 0f }
                    .entries
                    .sortedBy { it.key }
                    .map { entry ->
                        entry.key.format(DateTimeFormatter.ofPattern("dd")) to entry.value
                    }
            }
        }
    }

    val yearMonthlyStats by remember {
        derivedStateOf {
            if (sessions.isEmpty()) emptyList()
            else {
                val now = LocalDateTime.now()
                val currentYear = now.year

                val statsMap = sessions
                    .filter { it.timestamp.year == currentYear }
                    .groupBy { YearMonth.from(it.timestamp) }
                    .mapValues { entry ->
                        entry.value.sumOf { it.duration } / 60f
                    }

                statsMap
                    .filter { it.value > 0f }
                    .entries
                    .sortedBy { it.key }
                    .map { entry ->
                        val monthName = when (entry.key.monthValue) {
                            1 -> "1月"
                            2 -> "2月"
                            3 -> "3月"
                            4 -> "4月"
                            5 -> "5月"
                            6 -> "6月"
                            7 -> "7月"
                            8 -> "8月"
                            9 -> "9月"
                            10 -> "10月"
                            11 -> "11月"
                            12 -> "12月"
                            else -> ""
                        }
                        monthName to entry.value
                    }
            }
        }
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "统计",
                color = Color.Transparent,
                scrollBehavior = MiuixScrollBehavior()
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (sessions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "(。・ω・。)",
                        style = MiuixTheme.textStyles.title2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无统计数据哦！",
                        style = MiuixTheme.textStyles.body2,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    overscrollEffect = null
                ) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "最近一次",
                                    style = MiuixTheme.textStyles.title3,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val info = latestSessionInfo
                                if (info == null) {
                                    Text(
                                        text = "还没有开始记录哦～\n快去完成第一次吧！",
                                        style = MiuixTheme.textStyles.body1,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = info.displayDate,
                                        style = MiuixTheme.textStyles.title2,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${info.time} · 坚持了 ${formatDuration(info.durationSeconds)}",
                                        style = MiuixTheme.textStyles.body1,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = info.breakDetail,
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "总体统计",
                                    style = MiuixTheme.textStyles.title3,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val totalCount = totalStats.first
                                val totalSeconds = totalStats.second
                                val avgMinutes = totalStats.third

                                Text(
                                    text = formatDuration(totalSeconds),
                                    style = MiuixTheme.textStyles.title1,
                                    color = MiuixTheme.colorScheme.primary
                                )

                                val avgText =
                                    if (totalCount > 0) "%.1f 分钟".format(avgMinutes) else "0 分钟"
                                Text(
                                    text = "平均每次 $avgText · 共 $totalCount 次",
                                    style = MiuixTheme.textStyles.body1,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )

                                val statusText by remember(totalCount) {
                                    mutableStateOf(buildTotalStatStatus(totalCount))
                                }
                                Text(
                                    text = statusText,
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatCount(label = "本周", value = weekCount)
                                    StatCount(label = "本月", value = monthCount)
                                    StatCount(label = "今年", value = yearCount)
                                }
                            }
                        }
                    }

                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "本周",
                                    style = MiuixTheme.textStyles.title3
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                PeriodStatRow(
                                    totalSeconds = weekStats.first,
                                    avgMinutes = weekStats.second,
                                    totalLabel = "本周总时长"
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                BarChart(data = weekDailyStats)
                            }
                        }
                    }

                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "本月",
                                    style = MiuixTheme.textStyles.title3
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                PeriodStatRow(
                                    totalSeconds = monthStats.first,
                                    avgMinutes = monthStats.second,
                                    totalLabel = "本月总时长"
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                BarChart(data = monthDailyStats)
                            }
                        }
                    }

                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "今年",
                                    style = MiuixTheme.textStyles.title3
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                PeriodStatRow(
                                    totalSeconds = yearStats.first,
                                    avgMinutes = yearStats.second,
                                    totalLabel = "今年总时长"
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                BarChart(data = yearMonthlyStats)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCount(
    label: String,
    value: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MiuixTheme.textStyles.title3,
            color = MiuixTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

@Composable
private fun PeriodStatRow(
    totalSeconds: Int,
    avgMinutes: Float,
    totalLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val totalTextColor =
            if (totalSeconds > 0) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatDuration(totalSeconds),
                style = MiuixTheme.textStyles.title3,
                color = totalTextColor
            )
            Text(
                text = totalLabel,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }

        val avgText =
            if (totalSeconds > 0) "%.1f 分钟".format(avgMinutes) else "0 分钟"
        val avgTextColor =
            if (totalSeconds > 0) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = avgText,
                style = MiuixTheme.textStyles.title3,
                color = avgTextColor
            )
            Text(
                text = "平均每次",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 240.dp,
    minBarWidth: Dp = 16.dp,
    maxBarWidth: Dp = 54.dp,
    spacing: Dp = 16.dp
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "无数据",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
        return
    }

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1f)
    val barColor = MiuixTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight + 60.dp)
    ) {
        YAxis(
            maxValue = maxValue,
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
        )

        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            val totalSpacing = spacing * (data.size - 1)
            val availableWidth = maxWidth - totalSpacing
            val idealBarWidth = availableWidth / data.size
            val barWidth = idealBarWidth.coerceIn(minBarWidth, maxBarWidth)

            LazyRow(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .overScrollHorizontal(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                overscrollEffect = null
            ) {
                items(data) { (date, value) ->
                    BarItem(
                        value = value,
                        maxValue = maxValue,
                        date = date,
                        barWidth = barWidth,
                        chartHeight = chartHeight,
                        color = barColor
                    )
                }
            }
        }
    }
}

@Composable
private fun YAxis(
    maxValue: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "${maxValue.toInt()} 分钟",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
        Text(
            text = "${(maxValue / 2).toInt()} 分钟",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f)
        )
        Text(
            text = "0",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun BarItem(
    value: Float,
    maxValue: Float,
    date: String,
    barWidth: Dp,
    chartHeight: Dp,
    color: Color
) {
    val ratio = value / maxValue
    val barHeight = chartHeight * ratio

    Column(
        modifier = Modifier.width(barWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(chartHeight)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(barHeight)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(color)
            )

            val showInside = barHeight < 48.dp

            Text(
                text = value.toInt().toString(),
                style = MiuixTheme.textStyles.body2,
                color = if (showInside) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(
                        y = if (showInside)
                            -barHeight / 2
                        else
                            -barHeight - 8.dp
                    )
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = date,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

private data class DailyStat(
    val count: Int,
    val totalDuration: Int
)

private enum class PeriodType { WEEK, MONTH, YEAR }

private fun calculatePeriodStats(
    sessions: List<Session>,
    type: PeriodType,
    now: LocalDateTime
): Pair<Int, Float> {
    val filtered = sessions.filter { session ->
        when (type) {
            PeriodType.WEEK -> {
                val weekStart = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                session.timestamp >= weekStart
            }

            PeriodType.MONTH -> {
                val monthStart = now.withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                session.timestamp >= monthStart
            }

            PeriodType.YEAR -> {
                val yearStart = now.withDayOfYear(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                session.timestamp >= yearStart
            }
        }
    }

    if (filtered.isEmpty()) return 0 to 0f

    val totalSeconds = filtered.sumOf { it.duration }
    val avgMinutes = totalSeconds.toFloat() / (60 * filtered.size)
    return totalSeconds to avgMinutes
}

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) {
        "${hours}小时 ${minutes}分钟"
    } else {
        "${minutes}分钟"
    }
}

private fun buildTotalStatStatus(totalCount: Int): String {
    fun randomOf(vararg list: String) =
        list.random()

    return when {
        totalCount <= 0 -> "还没有留下任何记录"

        totalCount < 3 -> randomOf(
            "已经不是没开始，是在路上了",
            "第一次不重要，继续就好",
            "不是手滑，是开始了"
        )

        totalCount < 10 -> randomOf(
            "已经不是手滑，是习惯",
            "这已经算得上稳定输出了"
        )

        totalCount < 30 -> randomOf(
            "数据在这，不用自证",
            "已经不是一时兴起",
            "这事你是真的在做"
        )

        totalCount < 100 -> randomOf(
            "这是生活的一部分了",
            "不用提醒，你自己会来",
            "已经很难说是“记录”了"
        )

        else -> randomOf(
            "这不是习惯，这是你的一部分",
            "已经不需要证明任何东西",
            "数据只是顺手留下的痕迹"
        )
    }
}

private data class LatestSessionInfo(
    val daysAgo: Long,
    val displayDate: String,
    val time: String,
    val durationSeconds: Int,
    val breakDetail: String
)