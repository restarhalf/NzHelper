package me.restarhalf.deer.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.restarhalf.deer.R
import me.restarhalf.deer.ui.components.TwoTextButtonsRow
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.extra.WindowDropdown
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun RewardScreen(
    navController: NavController
) {
    Scaffold(
        popupHost = { },
        topBar = {
            TopAppBar(
                title = "赞赏作者",
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
        var selectedindex by remember { mutableIntStateOf(0) }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val showSaveDialog = remember { mutableStateOf(false) }
        var pendingSaveQr by remember { mutableStateOf<RewardQr?>(null) }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .overScrollVertical()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            overscrollEffect = null
        ) {
            item {
                Card {
                    WindowDropdown(
                        items = listOf("微信", "支付宝"),
                        title = "赞赏方式",
                        summary = "长按保存",
                        selectedIndex = selectedindex,
                        onSelectedIndexChange = { selectedindex = it }
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                val qr = when (selectedindex) {
                    0 -> RewardQr(
                        resId = R.drawable.reward_wechat,
                        mimeType = "image/png",
                        format = Bitmap.CompressFormat.PNG,
                        fileExtension = "png"
                    )

                    else -> RewardQr(
                        resId = R.drawable.reward_alipay,
                        mimeType = "image/jpeg",
                        format = Bitmap.CompressFormat.JPEG,
                        fileExtension = "jpg"
                    )
                }
                Card {
                    BasicComponent(
                        bottomAction = {
                            Image(
                                painter = painterResource(id = qr.resId),
                                contentDescription = "打赏二维码",
                                modifier = Modifier
                                    .size(400.dp)
                                    .pointerInput(qr.resId) {
                                        detectTapGestures(
                                            onLongPress = {
                                                pendingSaveQr = qr
                                                showSaveDialog.value = true
                                            }
                                        )
                                    }
                            )
                        }
                    )


                }
            }
        }

        WindowDialog(
            title = "保存图片",
            summary = "确认保存到相册？",
            show = showSaveDialog,
            onDismissRequest = {
                showSaveDialog.value = false
                pendingSaveQr = null
            }
        ) {
            TwoTextButtonsRow(
                leftText = "取消",
                onLeftClick = {
                    showSaveDialog.value = false
                    pendingSaveQr = null
                },
                rightText = "确认",
                onRightClick = {
                    val target = pendingSaveQr
                    showSaveDialog.value = false
                    pendingSaveQr = null
                    if (target != null) {
                        scope.launch {
                            val ok = runCatching { saveImageToGallery(context, target) }
                                .getOrDefault(false)
                            Toast.makeText(
                                context,
                                if (ok) "已保存" else "保存失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
            )
        }
    }
}

private data class RewardQr(
    val resId: Int,
    val mimeType: String,
    val format: Bitmap.CompressFormat,
    val fileExtension: String
)

private fun saveImageToGalleryLegacy(
    context: Context,
    bitmap: Bitmap,
    name: String,
    mimeType: String,
    format: Bitmap.CompressFormat
): Boolean {
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val dir = java.io.File(picturesDir, "DeerTimer")
    if (!dir.exists()) dir.mkdirs()
    val file = java.io.File(dir, name)
    return try {
        java.io.FileOutputStream(file).use { out ->
            val quality = if (format == Bitmap.CompressFormat.JPEG) 95 else 100
            bitmap.compress(format, quality, out)
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DATA, file.absolutePath)
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        true
    } catch (e: Exception) {
        false
    }
}

private suspend fun saveImageToGallery(context: Context, qr: RewardQr): Boolean {
    return withContext(Dispatchers.IO) {
        val bitmap = runCatching {
            BitmapFactory.decodeResource(context.resources, qr.resId)
        }.getOrNull() ?: return@withContext false

        val name = "reward_${System.currentTimeMillis()}.${qr.fileExtension}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, qr.mimeType)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/DeerTimer"
                )
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { out ->
                val quality = if (qr.format == Bitmap.CompressFormat.JPEG) 95 else 100
                bitmap.compress(qr.format, quality, out)
            } ?: return@withContext false

            true
        } else {
            saveImageToGalleryLegacy(context, bitmap, name, qr.mimeType, qr.format)
        }
    }
}