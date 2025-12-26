package me.restarhalf.deer.ui.miuix.screens


import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.restarhalf.deer.ui.md3.screens.LicenseItem
import me.restarhalf.deer.ui.md3.screens.LicenseType
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun OpenSourceScreen(
    navController: NavController
) {
    val context = LocalContext.current
    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "开放源代码",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = MiuixIcons.Useful.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .overScrollVertical()
                .verticalScroll(
                    rememberScrollState(),
                    overscrollEffect = null
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            licenseList.forEach { item ->
                Card {
                    BasicComponent(
                        title = "${item.name} - ${item.author}",
                        summary = item.url,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, item.url.toUri())
                            context.startActivity(intent)
                        }
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }
    }
}

private val licenseList = listOf(
    LicenseItem(
        "Google",
        "Activity Compose",
        "https://developer.android.com/jetpack/androidx/releases/activity",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "androidx.compose.material.icons",
        "https://developer.android.com/reference/kotlin/androidx/compose/material/icons/package-summary",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "androidx.compose.material3.windowsizeclass",
        "https://developer.android.com/reference/kotlin/androidx/compose/material3/windowsizeclass/package-summary",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "androidx.compose.ui.graphics",
        "https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/package-summary",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "androidx.compose.ui.tooling",
        "https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/package-summary",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "androidx.compose.ui.tooling.preview",
        "https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/package-summary",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Compose Material 3",
        "https://developer.android.com/jetpack/androidx/releases/compose-material3",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Compose Material 3 Adaptive",
        "https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Core",
        "https://developer.android.com/jetpack/androidx/releases/core",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Gson",
        "https://github.com/google/gson",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Jetpack Compose",
        "https://github.com/androidx/androidx",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Lifecycle",
        "https://developer.android.com/jetpack/androidx/releases/lifecycle",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Material Design 3",
        "https://m3.material.io/",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Navigation with Compose",
        "https://developer.android.com/develop/ui/compose/navigation",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Test",
        "https://developer.android.com/jetpack/androidx/releases/test",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Use a Bill of Materials",
        "https://developer.android.com/develop/ui/compose/bom",
        LicenseType.Apache2
    ),
    LicenseItem(
        "JetBrains",
        "Kotlin",
        "https://github.com/JetBrains/kotlin",
        LicenseType.Apache2
    ),
    LicenseItem(
        "square",
        "Moshi",
        "https://github.com/square/moshi",
        LicenseType.Apache2
    ),
    LicenseItem(
        "square",
        "okhttp",
        "https://github.com/square/okhttp",
        LicenseType.Apache2
    )
)

@Preview(showBackground = true)
@Composable
fun OpenSourceScreenPreview() {
    OpenSourceScreen(
        navController = rememberNavController()
    )
}