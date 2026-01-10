package me.restarhalf.deer.ui.screens


import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.restarhalf.deer.R
import me.restarhalf.deer.ui.custom.icons.GitHub
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Merge
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun AboutScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    val versionName = remember(context, isInPreview) {
        if (isInPreview) {
            "预览版"
        } else {
            try {
                context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionName
                    ?: "未知版本"
            } catch (_: PackageManager.NameNotFoundException) {
                "未知版本"
            }
        }
    }

    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "关于",
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
                .overScrollVertical()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            overscrollEffect = null
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                val context = LocalContext.current
                val icon = remember {
                    val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!
                    BitmapPainter(drawable.toBitmap().asImageBitmap())
                }
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MiuixTheme.textStyles.title2
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Text(
                    text = "版本: $versionName",
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                    style = MiuixTheme.textStyles.body2
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                )
            }

            item {
                Card {
                    BasicComponent(
                        startAction = {
                            Icon(
                                modifier = Modifier.padding(end = 16.dp),
                                imageVector = MiuixIcons.GitHub,
                                contentDescription = "仓库",
                                tint = MiuixTheme.colorScheme.onBackground
                            )
                        },
                        title = "仓库",
                        summary = "在GitHub仓库查看源码",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/restarhalf/DeerTimer".toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                Card {
                    BasicComponent(
                        startAction = {
                            Icon(
                                modifier = Modifier.padding(end = 16.dp),
                                imageVector = MiuixIcons.Merge,
                                contentDescription = "代码",
                                tint = MiuixTheme.colorScheme.onBackground
                            )
                        },
                        title = "开放源代码",
                        summary = "查看应用所使用的开源库及其许可证",
                        onClick = { navController.navigate("open_source") }
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                Card {
                    BasicComponent(
                        startAction = {
                            Icon(
                                modifier = Modifier.padding(end = 16.dp),
                                imageVector = MiuixIcons.Contacts,
                                contentDescription = "赞赏",
                                tint = MiuixTheme.colorScheme.onBackground
                            )
                        },
                        title = "赞赏作者",
                        summary = "暂时不需要，数据库太小了",
                        onClick = { navController.navigate("reward") }
                    )

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AboutScreen(
        navController = rememberNavController()
    )
}