package me.restarhalf.deer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.restarhalf.deer.data.ThemeRepository
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.WindowDropdown
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ThemeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs by ThemeRepository.themePreferences.collectAsState()
    val modeItems = listOf(
        "跟随系统",
        "浅色",
        "深色",
        "Monet 跟随系统",
        "Monet 浅色",
        "Monet 深色"
    )
    val modes = listOf(
        ColorSchemeMode.System,
        ColorSchemeMode.Light,
        ColorSchemeMode.Dark,
        ColorSchemeMode.MonetSystem,
        ColorSchemeMode.MonetLight,
        ColorSchemeMode.MonetDark
    )
    val modeIndex = modes.indexOf(prefs.miuixColorSchemeMode)
        .let { if (it >= 0) it else 0 }
    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "主题",
                color = Color.Transparent,
                scrollBehavior = MiuixScrollBehavior(),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card {
                WindowDropdown(
                    title = "底栏样式",
                    summary = "切换底栏展示样式",
                    items = listOf("固定", "悬浮"),
                    selectedIndex = if (prefs.miuixBottomBarFloating) 1 else 0,
                    onSelectedIndexChange = { index ->
                        ThemeRepository.setMiuixBottomBarFloating(context, index == 1)
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card {
                WindowDropdown(
                    title = "主题颜色",
                    summary = "选择颜色模式",
                    items = modeItems,
                    selectedIndex = modeIndex,
                    onSelectedIndexChange = { index ->
                        ThemeRepository.setMiuixColorSchemeMode(context, modes[index])
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card {
                SuperArrow(
                    title = "背景",
                    summary = "背景相关的一系列东西",
                    titleColor = BasicComponentDefaults.titleColor(),
                    summaryColor = BasicComponentDefaults.summaryColor(),
                    onClick = { navController.navigate("background") }
                )
            }

        }
    }
}