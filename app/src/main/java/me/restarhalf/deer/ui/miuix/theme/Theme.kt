package me.restarhalf.deer.ui.miuix.theme


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun NzHelperMiuixTheme(
    colorSchemeMode: ColorSchemeMode = ColorSchemeMode.System,
    content: @Composable () -> Unit
) {
    val keyColor: Color = colorScheme.primary
    val controller = remember(colorSchemeMode) {
        ThemeController(
            colorSchemeMode,
            keyColor = keyColor
        )
    }

    MiuixTheme(controller = controller) {
        content()
    }
}
