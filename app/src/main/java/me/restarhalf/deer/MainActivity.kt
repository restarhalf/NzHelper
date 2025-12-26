package me.restarhalf.deer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.data.UiStyle
import me.restarhalf.deer.ui.md3.theme.NzHelperTheme
import me.restarhalf.deer.ui.miuix.theme.NzHelperMiuixTheme
import me.restarhalf.deer.ui.md3.screens.MainScreen as Md3MainScreen
import me.restarhalf.deer.ui.miuix.screens.MainScreen as MiuixMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by ThemeRepository.themePreferences.collectAsState()

            when (prefs.uiStyle) {
                UiStyle.MD3 -> {
                    NzHelperTheme(darkTheme = prefs.md3DarkTheme) {
                        Md3MainScreen()
                    }
                }

                UiStyle.MIUIX -> {
                    NzHelperMiuixTheme(colorSchemeMode = prefs.miuixColorSchemeMode) {
                        MiuixMainScreen()
                    }
                }
            }
        }
    }
}