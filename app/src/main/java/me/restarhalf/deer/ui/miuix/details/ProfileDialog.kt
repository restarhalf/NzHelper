package me.restarhalf.deer.ui.miuix.details

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.imgbb.ImgbbRepository
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository
import me.restarhalf.deer.ui.util.AvatarCircle
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Personal
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProfileDialog(
    show: MutableState<Boolean>,
    onDismiss: () -> Unit
) {
    if (!show.value) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by SupabaseAuthRepository.session.collectAsState()

    var busy by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var nicknameText by remember(session?.nickname) { mutableStateOf(session?.nickname.orEmpty()) }

    val showEmailChangeDialog = remember { mutableStateOf(false) }
    val showResetPasswordDialog = remember { mutableStateOf(false) }

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null || session == null) return@rememberLauncherForActivityResult
        if (busy) return@rememberLauncherForActivityResult
        busy = true
        errorText = null
        scope.launch {
            try {
                val url = ImgbbRepository.uploadImage(context = context, uri = uri)
                SupabaseAuthRepository.updateMyProfile(
                    context = context,
                    avatarUrl = url
                )
            } catch (e: SupabaseApiException) {
                errorText = e.message
            } catch (e: Exception) {
                errorText = e.message ?: "上传失败"
            } finally {
                busy = false
            }
        }
    }

    EmailChangeOtpDialog(
        show = showEmailChangeDialog,
        onDismiss = { showEmailChangeDialog.value = false }
    )

    ResetPasswordOtpDialog(
        show = showResetPasswordDialog,
        onDismiss = { showResetPasswordDialog.value = false }
    )

    SuperDialog(
        title = "个人资料",
        titleColor = MiuixTheme.colorScheme.onSurface,
        show = show,
        enableWindowDim = true,
//        summary = session?.email?.takeIf { it.isNotBlank() } ?: "",
        summaryColor = MiuixTheme.colorScheme.onSurfaceSecondary,
        onDismissRequest = {
            show.value = false
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarText = (session?.nickname?.takeIf { it.isNotBlank() } ?: "?")
                    .first()
                    .uppercase()

                AvatarCircle(
                    avatarUrl = session?.avatarUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(enabled = !busy && session != null) {
                            pickAvatarLauncher.launch("image/*")
                        },
                    containerColor = MiuixTheme.colorScheme.secondaryContainer,
                    contentDescription = "头像"
                ) {
                    Text(
                        text = avatarText,
                        color = MiuixTheme.colorScheme.onSecondaryContainer,
                        style = MiuixTheme.textStyles.title2
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = session?.nickname ?: "",
                        style = MiuixTheme.textStyles.title2
                    )
                    if (!session?.email.isNullOrBlank()) {
                        Text(
                            text = session?.email ?: "",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

//            TextButton(
//                text = if (busy) "上传中..." else "更换头像",
//                enabled = !busy && session != null,
//                onClick = {
//                    if (busy || session == null) return@TextButton
//                    pickAvatarLauncher.launch("image/*")
//                }
//            )

            TextField(
                value = nicknameText,
                onValueChange = {
                    nicknameText = it
                    errorText = null
                },
                label = "昵称",
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !busy && session != null,
                leadingIcon = {
                    Icon(
                        imageVector = MiuixIcons.Useful.Personal,
                        contentDescription = "昵称",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        tint = MiuixTheme.colorScheme.onSecondaryContainer
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorText ?: "",
                    color = MiuixTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    text = "更改邮箱",
                    onClick = {
                        if (busy || session == null) return@TextButton
                        showEmailChangeDialog.value = true
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer( modifier = Modifier.size(16.dp))
                TextButton(
                    text = "重置密码",
                    onClick = {
                        if (busy || session == null) return@TextButton
                        showResetPasswordDialog.value = true
                    },
                    modifier = Modifier.weight(1f)
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    text = "取消",
                    onClick = {
                        show.value = false
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextButton(
                    text = if (busy) "保存中..." else "保存",
                    enabled = !busy && session != null,
                    onClick = {
                        if (busy) return@TextButton
                        busy = true
                        errorText = null
                        scope.launch {
                            try {
                                SupabaseAuthRepository.updateMyNickname(context, nicknameText)
                                show.value = false
                                onDismiss()
                            } catch (e: SupabaseApiException) {
                                errorText = e.message
                            } catch (e: Exception) {
                                errorText = e.message ?: "保存失败"
                            } finally {
                                busy = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}
