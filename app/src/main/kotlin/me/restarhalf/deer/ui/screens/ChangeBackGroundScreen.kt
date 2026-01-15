package me.restarhalf.deer.ui.screens


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.ui.components.TitleSlider
import me.restarhalf.deer.ui.util.CropConfig
import me.restarhalf.deer.ui.util.rememberImageCropLauncher
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ChangeBackGroundScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val prefs by ThemeRepository.themePreferences.collectAsState()
    val backgroundAlpha = prefs.backgroundAlpha
    val backgroundAmbiguity = prefs.backgroundAmbiguity
    val backgroundImageUri = prefs.backgroundImageUri
    val componentsAlpha = prefs.componentsAlpha
    val screenCropConfig = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        CropConfig(
            aspectRatioX = configuration.screenWidthDp.toFloat(),
            aspectRatioY = configuration.screenHeightDp.toFloat(),
            maxWidth = 2048,
            maxHeight = 2048,
            freeStyleCrop = false,
            toolbarTitle = "裁切背景"
        )
    }
    val pickBackgroundLauncher = rememberImageCropLauncher(
        cropConfig = screenCropConfig
    ) { uri: Uri? ->
        uri ?: return@rememberImageCropLauncher

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }

        ThemeRepository.setBackgroundImageUri(context, uri.toString())
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "更换背景",
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
    ) { it ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card {
                BasicComponent(
                    title = "更换背景",
                    summary = if (backgroundImageUri.isNullOrBlank()) {
                        "点击以更换背景"
                    } else {
                        "已选择背景图片"
                    },
                    onClick = {
                        pickBackgroundLauncher.launch()
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card {
                BasicComponent(
                    title = "清除背景",
                    summary = "清除当前背景",
                    enabled = !backgroundImageUri.isNullOrBlank(),
                    onClick = { ThemeRepository.setBackgroundImageUri(context, null) }
                )
            }
            Spacer(Modifier.height(12.dp))
            Card {
                TitleSlider(
                    title = "背景透明度",
                    value = backgroundAlpha,
                    onValueChange = { ThemeRepository.setBackgroundAlpha(context, it) },
                    valueRange = 0f..1f
                )
            }
            Spacer(Modifier.height(12.dp))
            Card {
                TitleSlider(
                    title = "背景模糊度",
                    value = backgroundAmbiguity,
                    onValueChange = { ThemeRepository.setBackgroundAmbiguity(context, it) },
                    valueRange = 0f..1f
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card {
                TitleSlider(
                    title = "组件透明度",
                    value = componentsAlpha,
                    onValueChange = { ThemeRepository.setComponentsAlpha(context, it) },
                    valueRange = 0f..1f
                )
            }

        }
    }
}