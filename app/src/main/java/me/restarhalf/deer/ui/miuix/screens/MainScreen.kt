package me.restarhalf.deer.ui.miuix.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.restarhalf.deer.BuildConfig
import me.restarhalf.deer.ui.miuix.BottomNavItem
import me.restarhalf.deer.ui.miuix.screens.statistics.StatisticsScreen
import me.restarhalf.deer.ui.util.UpdateChecker
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog

@Composable
private fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = BottomNavItem.items
    val navItems = items.map { NavigationItem(it.title, it.icon) }
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }
        .let { if (it >= 0) it else 0 }

    NavigationBar(
        items = navItems,
        selected = selectedIndex,
        onClick = { index ->
            navController.navigate(items[index].route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val showNotifyDialog = androidx.compose.runtime.remember {
        mutableStateOf(!notificationsEnabled)
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra("app_uid", context.applicationInfo.uid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val owner = "restarhalf"
    val repo = "NzHelper"

    val showUpdateDialog = androidx.compose.runtime.remember { mutableStateOf(false) }
    var latestTag by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }

    fun stripSuffix(version: String): String =
        version.trimStart('v', 'V').substringBefore('-')

    fun parseNumbers(version: String): List<Int> =
        stripSuffix(version)
            .split('.')
            .map { it.toIntOrNull() ?: 0 }
            .let {
                when {
                    it.size >= 3 -> it.take(3)
                    it.size == 2 -> it + listOf(0)
                    it.size == 1 -> it + listOf(0, 0)
                    else -> listOf(0, 0, 0)
                }
            }

    fun isRemoteGreater(local: String, remote: String): Boolean {
        val localNums = parseNumbers(local)
        val remoteNums = parseNumbers(remote)
        for (i in 0..2) {
            if (remoteNums[i] > localNums[i]) return true
            if (remoteNums[i] < localNums[i]) return false
        }
        return false
    }

    LaunchedEffect(Unit) {
        UpdateChecker.fetchLatestVersion(owner, repo)?.let { remoteVer ->
            latestTag = remoteVer
            if (isRemoteGreater(BuildConfig.VERSION_NAME, remoteVer)) {
                showUpdateDialog.value = true
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val initialIndex = BottomNavItem.items.indexOfFirst { it.route == initialState.destination.route }
                val targetIndex = BottomNavItem.items.indexOfFirst { it.route == targetState.destination.route }
                val direction = when {
                    initialIndex >= 0 && targetIndex >= 0 -> if (targetIndex > initialIndex) SlideDirection.Left else SlideDirection.Right
                    else -> SlideDirection.Left
                }
                slideIntoContainer(direction, tween(260)) + fadeIn(tween(260))
            },
            exitTransition = {
                val initialIndex = BottomNavItem.items.indexOfFirst { it.route == initialState.destination.route }
                val targetIndex = BottomNavItem.items.indexOfFirst { it.route == targetState.destination.route }
                val direction = when {
                    initialIndex >= 0 && targetIndex >= 0 -> if (targetIndex > initialIndex) SlideDirection.Left else SlideDirection.Right
                    else -> SlideDirection.Left
                }
                slideOutOfContainer(direction, tween(260)) + fadeOut(tween(260))
            },
            popEnterTransition = {
                slideIntoContainer(SlideDirection.Right, tween(260)) + fadeIn(tween(260))
            },
            popExitTransition = {
                slideOutOfContainer(SlideDirection.Right, tween(260)) + fadeOut(tween(260))
            }
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Statistics.route) { StatisticsScreen() }
            composable(BottomNavItem.History.route) { HistoryScreen() }
            composable(BottomNavItem.Settings.route) { SettingsScreen(navController) }
            composable("about") { AboutScreen(navController) }
            composable("open_source") { OpenSourceScreen(navController) }
        }

        if (showUpdateDialog.value && latestTag != null) {
            SuperDialog(
                title = "检测到新版本",
                summary =
                    "当前版本：${BuildConfig.VERSION_NAME}\n" +
                        "最新版本：$latestTag\n\n" +
                        "针对你的牛牛进行了一些优化，是否前往 GitHub 下载？",
                show = showUpdateDialog,
                onDismissRequest = { showUpdateDialog.value = false }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        text = "稍后再说",
                        onClick = { showUpdateDialog.value = false },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        text = "去下载",
                        onClick = {
                            showUpdateDialog.value = false
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/$owner/$repo/releases/latest".toUri()
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }

        SuperDialog(
            title = "还未开启通知权限",
            summary = "为确保应用能在后台继续计时，请授予通知权限！",
            show = showNotifyDialog,
            onDismissRequest = { showNotifyDialog.value = false }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    text = "以后再说",
                    onClick = { showNotifyDialog.value = false },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    text = "去开启",
                    onClick = {
                        openNotificationSettings(context)
                        showNotifyDialog.value = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
