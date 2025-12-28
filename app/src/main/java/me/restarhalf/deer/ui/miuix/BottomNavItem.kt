package me.restarhalf.deer.ui.miuix

import androidx.compose.ui.graphics.vector.ImageVector
import me.restarhalf.deer.ui.miuix.icons.custom.Rank
import me.restarhalf.deer.ui.miuix.icons.custom.Stat
import me.restarhalf.deer.ui.miuix.icons.custom.Timer
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Restore
import top.yukonga.miuix.kmp.icon.icons.useful.Settings

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "计时",
        icon = Timer
    )

    object Statistics : BottomNavItem(
        route = "statistics",
        title = "统计",
        icon = Stat
    )

    object Histories : BottomNavItem(
        route = "history",
        title = "历史",
        icon = MiuixIcons.Useful.Restore
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "设置",
        icon = MiuixIcons.Useful.Settings
    )

    object Rankings : BottomNavItem(
        route = "rankings",
        title = "排行榜",
        icon = Rank
    )

    companion object {
        val items = listOf(Home, Rankings, Statistics, Histories, Settings)
    }
}