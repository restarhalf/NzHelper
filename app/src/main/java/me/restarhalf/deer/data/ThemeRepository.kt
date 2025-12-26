package me.restarhalf.deer.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.yukonga.miuix.kmp.theme.ColorSchemeMode

enum class UiStyle {
    MD3,
    MIUIX
}

data class ThemePreferences(
    val uiStyle: UiStyle = UiStyle.MD3,
    val md3DarkTheme: Boolean = false,
    val miuixColorSchemeMode: ColorSchemeMode = ColorSchemeMode.System
)

object ThemeRepository {
    private const val PREFS_NAME = "theme_prefs"

    private const val KEY_UI_STYLE = "ui_style"
    private const val KEY_MD3_DARK_THEME = "md3_dark_theme"
    private const val KEY_MIUIX_COLOR_SCHEME_MODE = "miuix_color_scheme_mode"

    private val _themePreferences = MutableStateFlow(ThemePreferences())
    val themePreferences: StateFlow<ThemePreferences> = _themePreferences.asStateFlow()

    fun init(context: Context) {
        _themePreferences.value = load(context)
    }

    fun setUiStyle(context: Context, uiStyle: UiStyle) {
        update(context) { it.copy(uiStyle = uiStyle) }
    }

    fun setMd3DarkTheme(context: Context, darkTheme: Boolean) {
        update(context) { it.copy(md3DarkTheme = darkTheme) }
    }

    fun setMiuixColorSchemeMode(context: Context, mode: ColorSchemeMode) {
        update(context) { it.copy(miuixColorSchemeMode = mode) }
    }

    private fun update(context: Context, block: (ThemePreferences) -> ThemePreferences) {
        val newValue = block(_themePreferences.value)
        _themePreferences.value = newValue
        save(context, newValue)
    }

    private fun load(context: Context): ThemePreferences {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val uiStyleStr = prefs.getString(KEY_UI_STYLE, UiStyle.MD3.name)
        val md3DarkTheme = prefs.getBoolean(KEY_MD3_DARK_THEME, false)
        val miuixModeStr = prefs.getString(KEY_MIUIX_COLOR_SCHEME_MODE, ColorSchemeMode.System.name)

        val uiStyle = runCatching { UiStyle.valueOf(uiStyleStr ?: UiStyle.MD3.name) }
            .getOrDefault(UiStyle.MD3)

        val miuixMode = runCatching { ColorSchemeMode.valueOf(miuixModeStr ?: ColorSchemeMode.System.name) }
            .getOrDefault(ColorSchemeMode.System)

        return ThemePreferences(
            uiStyle = uiStyle,
            md3DarkTheme = md3DarkTheme,
            miuixColorSchemeMode = miuixMode
        )
    }

    private fun save(context: Context, prefs: ThemePreferences) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit {
            putString(KEY_UI_STYLE, prefs.uiStyle.name)
            putBoolean(KEY_MD3_DARK_THEME, prefs.md3DarkTheme)
            putString(KEY_MIUIX_COLOR_SCHEME_MODE, prefs.miuixColorSchemeMode.name)
        }
    }
}
