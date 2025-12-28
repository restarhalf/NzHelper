package me.restarhalf.deer.ui.miuix.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.data.UiStyle
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository
import me.restarhalf.deer.ui.miuix.details.LoginDialog
import me.restarhalf.deer.ui.miuix.details.ProfileDialog
import me.restarhalf.deer.ui.miuix.details.SignupDialog
import me.restarhalf.deer.ui.util.AvatarCircle
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs by ThemeRepository.themePreferences.collectAsState()

    val scope = rememberCoroutineScope()
    val session by SupabaseAuthRepository.session.collectAsState()
    var authBusy by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    val showLoginDialog = remember { mutableStateOf(false) }
    val showSignupDialog = remember { mutableStateOf(false) }
    val showProfileDialog = remember { mutableStateOf(false) }
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
    ) {

        LoginDialog(
            show = showLoginDialog,
            onConfirm = { email, password ->
                authError = null
                authBusy = true
                scope.launch {
                    try {
                        SupabaseAuthRepository.signInWithPassword(context, email.trim(), password)
                    } catch (e: SupabaseApiException) {
                        authError = e.message
                    } catch (e: Exception) {
                        authError = e.message ?: "登录失败"
                    } finally {
                        authBusy = false
                    }
                }
            },
            onDismiss = { showLoginDialog.value = false },
            onSignup = {
                showLoginDialog.value = false
                showSignupDialog.value = true
            }
        )
        SignupDialog(
            show = showSignupDialog,
            onConfirm = { email, password, nickname ->
                authError = null
                authBusy = true
                scope.launch {
                    try {
                        val session = SupabaseAuthRepository.signUpWithEmail(
                            context,
                            nickname,
                            email.trim(),
                            password
                        )
                        if (session == null) {
                            authError = "注册成功，请前往邮箱验证后再登录"
                        }
                    } catch (e: SupabaseApiException) {
                        authError = e.message
                    } catch (e: Exception) {
                        authError = e.message ?: "注册失败"
                    } finally {
                        authBusy = false
                    }
                }
            },
            onDismiss = { showSignupDialog.value = false },
            onCancel = {
                showSignupDialog.value = false
                showLoginDialog.value = true
            }
        )

        ProfileDialog(
            show = showProfileDialog,
            onDismiss = {
                showProfileDialog.value = false
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (session == null) {
                Card {
                    BasicComponent(
                        title = "登录",
                        summary = when {
                            authBusy -> "处理中..."

                            else -> "登录到您的账户"
                        },
                        enabled = !authBusy,
                        onClick = {
                            authError = null
                            showLoginDialog.value = true
                        }
                    )
                }
            } else {
                val avatarText = session?.nickname
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.first()
                    ?.uppercase()
                    ?: "?"

                Card {
                    BasicComponent(
                        title = session?.nickname,
                        summary = session?.email,
                        //头像
                        leftAction = {
                            AvatarCircle(
                                avatarUrl = session?.avatarUrl,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(40.dp),
                                containerColor = MiuixTheme.colorScheme.secondaryContainer,
                                contentDescription = "头像"
                            ) {
                                Text(
                                    text = avatarText,
                                    color = MiuixTheme.colorScheme.onSecondaryContainer,
                                    style = MiuixTheme.textStyles.title2
                                )
                            }
                        },
                        onClick = {
                            showProfileDialog.value = true;
                        }
                    )
                }
            }
            if (authError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authError ?: "",
                    color = colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.weight(1f))
            if (session != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card {
                    BasicComponent(
                        title = "退出登录",
                        summary = "清除本地登录状态",
                        enabled = !authBusy,
                        onClick = {
                            authError = null
                            SupabaseAuthRepository.signOut(context)
                        }
                    )
                }
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
