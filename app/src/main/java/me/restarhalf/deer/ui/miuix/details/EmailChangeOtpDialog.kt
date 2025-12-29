package me.restarhalf.deer.ui.miuix.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun EmailChangeOtpDialog(
    show: MutableState<Boolean>,
    onDismiss: () -> Unit
) {
    if (!show.value) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by SupabaseAuthRepository.session.collectAsState()

    var busy by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf<String?>(null) }
    var newEmailText by remember { mutableStateOf("") }
    var tokenText by remember { mutableStateOf("") }

    SuperDialog(
        title = "更改邮箱",
        titleColor = MiuixTheme.colorScheme.onSurface,
        show = show,
        enableWindowDim = true,
        summary = "输入新邮箱并填写验证码。",
        summaryColor = MiuixTheme.colorScheme.onSurfaceSecondary,
        onDismissRequest = {
            if (busy) return@SuperDialog
            show.value = false
            onDismiss()
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = newEmailText,
                onValueChange = {
                    newEmailText = it
                    errorText = null
                    messageText = null
                },
                label = "新邮箱",
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = tokenText,
                onValueChange = {
                    tokenText = it
                    errorText = null
                    messageText = null
                },
                label = "验证码",
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (messageText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = messageText ?: "",
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
            }
            if (errorText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText ?: "",
                    color = MiuixTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    text = if (busy) "发送中..." else "发送验证码",
                    enabled = !busy && session != null,
                    colors = ButtonDefaults.textButtonColors(),
                    onClick = {
                        if (busy || session == null) return@TextButton
                        busy = true
                        errorText = null
                        messageText = null
                        scope.launch {
                            try {
                                SupabaseAuthRepository.requestEmailChange(
                                    context = context,
                                    newEmail = newEmailText
                                )
                                messageText = "验证码已发送"
                            } catch (e: SupabaseApiException) {
                                errorText = e.message
                            } catch (e: Exception) {
                                errorText = e.message ?: "发送验证码失败"
                            } finally {
                                busy = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextButton(
                    text = if (busy) "处理中..." else "确认更改",
                    enabled = !busy && session != null,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        if (busy || session == null) return@TextButton
                        busy = true
                        errorText = null
                        messageText = null
                        scope.launch {
                            try {
                                SupabaseAuthRepository.verifyEmailOtp(
                                    context = context,
                                    email = newEmailText,
                                    token = tokenText,
                                    type = "email_change"
                                )
                                show.value = false
                                onDismiss()
                            } catch (e: SupabaseApiException) {
                                errorText = e.message
                            } catch (e: Exception) {
                                errorText = e.message ?: "更改邮箱失败"
                            } finally {
                                busy = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
