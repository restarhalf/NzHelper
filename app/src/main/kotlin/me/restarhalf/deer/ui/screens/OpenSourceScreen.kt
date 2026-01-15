package me.restarhalf.deer.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import me.restarhalf.deer.R
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .overScrollVertical()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            overscrollEffect = null
        ) {
            items(licenseList) { item ->
                Card {
                    BasicComponent(
                        startAction = {
                            Column(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .width(72.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                GetIcon(item.author)
                                Text(
                                    text = item.author,
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        title = item.name,
                        summary = getLicense(item.type),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, item.url.toUri())
                            context.startActivity(intent)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private val licenseList = listOf(
    LicenseItem(
        "Google",
        "AndroidX Core KTX",
        "https://developer.android.com/jetpack/androidx/releases/core",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "AndroidX AppCompat",
        "https://developer.android.com/jetpack/androidx/releases/appcompat",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "AndroidX Lifecycle Runtime KTX",
        "https://developer.android.com/jetpack/androidx/releases/lifecycle",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "AndroidX Activity Compose",
        "https://developer.android.com/jetpack/androidx/releases/activity",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Jetpack Compose BOM",
        "https://developer.android.com/develop/ui/compose/bom",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Jetpack Compose UI",
        "https://developer.android.com/jetpack/androidx/releases/compose-ui",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Navigation Compose",
        "https://developer.android.com/jetpack/androidx/releases/navigation",
        LicenseType.Apache2
    ),
    LicenseItem(
        "square",
        "OkHttp",
        "https://github.com/square/okhttp",
        LicenseType.Apache2
    ),
    LicenseItem(
        "square",
        "Moshi",
        "https://github.com/square/moshi",
        LicenseType.Apache2
    ),
    LicenseItem(
        "JetBrains",
        "Kotlin",
        "https://github.com/JetBrains/kotlin",
        LicenseType.Apache2
    ),
    LicenseItem(
        "MIUIX",
        "miuix",
        "https://github.com/compose-miuix-ui/miuix",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Yalantis",
        "uCrop",
        "https://github.com/Yalantis/uCrop",
        LicenseType.Apache2
    ),
    LicenseItem(
        "LuckSiege",
        "PictureSelector",
        "https://github.com/LuckSiege/PictureSelector",
        LicenseType.Apache2
    ),
    LicenseItem(
        "coil",
        "Coil3",
        "https://github.com/coil-kt/coil",
        LicenseType.Apache2
    )
)

data class LicenseItem(
    val author: String,
    val name: String,
    val url: String,
    val type: LicenseType
)

enum class LicenseType {
    Apache2,
    MIT,
    GPL3
}

private fun getLicense(type: LicenseType): String =
    when (type) {
        LicenseType.Apache2 -> "Apache Software License 2.0"
        LicenseType.MIT -> "MIT License"
        LicenseType.GPL3 -> "GNU general public license Version 3"
    }

@Composable
private fun GetIcon(author: String) {
    when (author) {
        "Google" -> Icon(
            painterResource(id = R.drawable.google),
            contentDescription = null,
            tint = Color.Unspecified
        )

        "JetBrains" -> Icon(
            painterResource(id = R.drawable.jetbrains),
            contentDescription = null,
            tint = Color.Unspecified
        )

        "square" -> Icon(
            painterResource(id = R.drawable.square),
            contentDescription = null,
            tint = Color.Unspecified
        )

        "MIUIX" -> Icon(
            painterResource(id = R.drawable.miuix),
            contentDescription = null,
            tint = Color.Unspecified
        )

        "Yalantis" -> Icon(
            painterResource(id = R.drawable.yalantis),
            contentDescription = null,
            tint = Color.Unspecified
        )


        "coil" -> Icon(
            painterResource(id = R.drawable.coil),
            contentDescription = null,
            tint = Color.Unspecified
        )

        else -> Icon(
            painterResource(id = R.drawable.code_24px),
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onBackground
        )
    }
}