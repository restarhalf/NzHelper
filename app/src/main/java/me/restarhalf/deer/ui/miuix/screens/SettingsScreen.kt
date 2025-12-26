package me.restarhalf.deer.ui.miuix.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.data.UiStyle
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.theme.ColorSchemeMode

@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs by ThemeRepository.themePreferences.collectAsState()

    val uiStyleItems = listOf("Material Design 3", "MIUIX")
    val uiStyleIndex = when (prefs.uiStyle) {
        UiStyle.MD3 -> 0
        UiStyle.MIUIX -> 1
    }

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
                title = "设置"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card {
                SuperDropdown(
                    title = "界面风格",
                    summary = "选择主题",
                    items = uiStyleItems,
                    selectedIndex = uiStyleIndex,
                    onSelectedIndexChange = { index ->
                        val newStyle = if (index == 0) UiStyle.MD3 else UiStyle.MIUIX
                        ThemeRepository.setUiStyle(context, newStyle)
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card {
                SuperDropdown(
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
                BasicComponent(
                    title = "关于",
                    summary = "版本信息与开源许可",
                    onClick = { navController.navigate("about") }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        navController = rememberNavController()
    )
}
