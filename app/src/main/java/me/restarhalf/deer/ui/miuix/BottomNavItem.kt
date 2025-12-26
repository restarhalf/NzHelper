package me.restarhalf.deer.ui.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import me.restarhalf.deer.R
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Order
import top.yukonga.miuix.kmp.icon.icons.useful.Play
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
        icon = MiuixIcons.Useful.Play
    )

    object Statistics : BottomNavItem(
        route = "statistics",
        title = "统计",
        icon = MiuixIcons.Useful.Order
    )

    object History : BottomNavItem(
        route = "history",
        title = "历史",
        icon = MiuixIcons.Useful.Restore
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "设置",
        icon = MiuixIcons.Useful.Settings
    )

    companion object {
        val items = listOf(Home, Statistics, History, Settings)
    }
}