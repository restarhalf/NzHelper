package me.restarhalf.deer.ui.md3

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import me.restarhalf.deer.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: @Composable () -> Painter
) {
    object Home : BottomNavItem(
        route = "home",
        title = "计时",
        icon = { painterResource(id = R.drawable.timer_24px) }
    )

    object Statistics : BottomNavItem(
        route = "statistics",
        title = "统计",
        icon = { painterResource(id = R.drawable.waterfall_chart_24px) }
    )

    object Histories : BottomNavItem(
        route = "history",
        title = "历史",
        icon = { painterResource(id = R.drawable.history_24px) }
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "设置",
        icon = { painterResource(id = R.drawable.settings_24px) }
    )

    object Rank : BottomNavItem(
        route = "rank",
        title = "排名",
        icon = { painterResource(id = R.drawable.rank_24px) }
    )

    companion object {
        val items = listOf(Home, Rank, Statistics, Histories, Settings)
    }
}