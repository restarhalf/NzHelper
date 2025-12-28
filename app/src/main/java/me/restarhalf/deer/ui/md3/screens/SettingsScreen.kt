package me.restarhalf.deer.ui.md3.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import me.restarhalf.deer.ui.md3.details.LoginDialog
import me.restarhalf.deer.ui.md3.details.ProfileDialog
import me.restarhalf.deer.ui.md3.details.SignupDialog
import me.restarhalf.deer.ui.util.AvatarCircle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs by ThemeRepository.themePreferences.collectAsState()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val scope = rememberCoroutineScope()
    val session by SupabaseAuthRepository.session.collectAsState()
    var authBusy by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    var showLoginDialog by remember { mutableStateOf(false) }
    var showSignupDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val md3Selected = prefs.uiStyle == UiStyle.MD3
    val miuixSelected = prefs.uiStyle == UiStyle.MIUIX

    val md3ContainerColor by animateColorAsState(
        targetValue = if (md3Selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    )
    val miuixContainerColor by animateColorAsState(
        targetValue = if (miuixSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    )

    val md3DotColor by animateColorAsState(
        targetValue = if (md3Selected) MaterialTheme.colorScheme.primary else Color.Transparent
    )
    val miuixDotColor by animateColorAsState(
        targetValue = if (miuixSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    )

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("设置") },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
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
            onDismiss = { showLoginDialog = false },
            onSignup = {
                showLoginDialog = false
                showSignupDialog = true
            }
        )

        SignupDialog(
            show = showSignupDialog,
            onConfirm = { email, password, nickname ->
                authError = null
                authBusy = true
                scope.launch {
                    try {
                        val newSession = SupabaseAuthRepository.signUpWithEmail(
                            context = context,
                            nickname = nickname,
                            email = email.trim(),
                            password = password
                        )
                        if (newSession == null) {
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
            onDismiss = { showSignupDialog = false },
            onCancel = {
                showSignupDialog = false
                showLoginDialog = true
            }
        )

        ProfileDialog(
            show = showProfileDialog,
            onDismiss = { showProfileDialog = false }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val curSession = session
            if (curSession == null) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    enabled = !authBusy,
                    onClick = {
                        authError = null
                        showLoginDialog = true
                    }
                ) {
                    ListItem(
                        leadingContent = {
                            Spacer(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Transparent, CircleShape)
                            )
                        },
                        headlineContent = {
                            Text(text = "登录", style = MaterialTheme.typography.titleMedium)
                        },
                        supportingContent = {
                            Text(text = if (authBusy) "处理中..." else "登录以使用排行榜")
                        }
                    )
                }
            } else {
                val avatarText = curSession.nickname
                    .trim()
                    .takeIf { it.isNotBlank() }
                    ?.first()
                    ?.uppercase()
                    ?: "?"

                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    enabled = !authBusy,
                    onClick = {
                        authError = null
                        showProfileDialog = true
                    }
                ) {
                    ListItem(
                        leadingContent = {
                            AvatarCircle(
                                avatarUrl = curSession.avatarUrl,
                                modifier = Modifier.size(40.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentDescription = "头像"
                            ) {
                                Text(
                                    text = avatarText,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = curSession.nickname,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Text(text = curSession.email ?: "查看和编辑个人资料")
                        }
                    )
                }
            }

            if (authError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = md3ContainerColor
                ),
                onClick = { ThemeRepository.setUiStyle(context, UiStyle.MD3) }
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(
                            text = "Material Design 3",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    supportingContent = {
                        Text(text = "Material Design 3 界面")
                    },
                    trailingContent = {
                        Spacer(
                            modifier = Modifier
                                .size(10.dp)
                                .background(md3DotColor, CircleShape)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = miuixContainerColor
                ),
                onClick = { ThemeRepository.setUiStyle(context, UiStyle.MIUIX) }
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                        )
                    },
                    headlineContent = {
                        Text(text = "MIUIX", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text(text = "miuix 风格界面")
                    },
                    trailingContent = {
                        Spacer(
                            modifier = Modifier
                                .size(10.dp)
                                .background(miuixDotColor, CircleShape)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                headlineContent = {
                    Text(text = "深色模式", style = MaterialTheme.typography.titleMedium)
                },
                supportingContent = {
                    Text(text = "仅对 Material Design 3 界面生效")
                },
                trailingContent = {
                    Switch(
                        checked = prefs.md3DarkTheme,
                        onCheckedChange = { ThemeRepository.setMd3DarkTheme(context, it) }
                    )
                }
            )
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("about")
                    },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(text = "关于", style = MaterialTheme.typography.titleMedium)
                }
            )
            Spacer(modifier = Modifier.weight(1f))

            if (session != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    enabled = !authBusy,
                    onClick = {
                        authError = null
                        SupabaseAuthRepository.signOut(context)
                    }
                ) {
                    ListItem(
                        headlineContent = {
                            Text(text = "退出登录", style = MaterialTheme.typography.titleMedium)
                        },
                        supportingContent = {
                            Text(text = "清除本地登录状态")
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
