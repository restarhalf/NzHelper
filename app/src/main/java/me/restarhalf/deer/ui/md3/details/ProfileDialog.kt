package me.restarhalf.deer.ui.md3.details

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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

@Composable
fun ProfileDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by SupabaseAuthRepository.session.collectAsState()

    var busy by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var nicknameText by remember(session?.nickname) { mutableStateOf(session?.nickname.orEmpty()) }

    var showEmailChangeDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }

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
            } catch (t: Throwable) {
                errorText = t.message ?: "上传失败"
            } finally {
                busy = false
            }
        }
    }

    EmailChangeOtpDialog(
        show = showEmailChangeDialog,
        onDismiss = { showEmailChangeDialog = false }
    )

    ResetPasswordOtpDialog(
        show = showResetPasswordDialog,
        onDismiss = { showResetPasswordDialog = false }
    )

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = { Text("个人资料") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentDescription = "头像"
                    ) {
                        Text(
                            text = avatarText,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session?.nickname ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!session?.email.isNullOrBlank()) {
                            Text(
                                text = session?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

//                TextButton(
//                    onClick = {
//                        if (busy || session == null) return@TextButton
//                        pickAvatarLauncher.launch("image/*")
//                    },
//                    enabled = !busy && session != null
//                ) {
//                    Text(if (busy) "上传中..." else "更换头像")
//                }

                OutlinedTextField(
                    value = nicknameText,
                    onValueChange = {
                        nicknameText = it
                        errorText = null
                    },
                    label = { Text("昵称") },
                    singleLine = true,
                    enabled = !busy && session != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            if (busy || session == null) return@TextButton
                            showEmailChangeDialog = true
                        },
                        enabled = !busy && session != null
                    ) {
                        Text("更改邮箱")
                    }
                    TextButton(
                        onClick = {
                            if (busy || session == null) return@TextButton
                            showResetPasswordDialog = true
                        },
                        enabled = !busy && session != null
                    ) {
                        Text("重置密码")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && session != null,
                onClick = {
                    if (busy) return@TextButton
                    busy = true
                    errorText = null
                    scope.launch {
                        try {
                            SupabaseAuthRepository.updateMyNickname(context, nicknameText)
                            onDismiss()
                        } catch (e: SupabaseApiException) {
                            errorText = e.message
                        } catch (e: Exception) {
                            errorText = e.message ?: "保存失败"
                        } finally {
                            busy = false
                        }
                    }
                }
            ) {
                Text(if (busy) "保存中..." else "保存")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text("取消")
                }
            }
        }
    )
}
