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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository.signOut
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ResetPasswordOtpDialog(
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

    var emailText by remember { mutableStateOf(session?.email.orEmpty()) }
    var tokenText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var passwordConfirmText by remember { mutableStateOf("") }

    SuperDialog(
        title = "重置密码",
        titleColor = MiuixTheme.colorScheme.onSurface,
        show = show,
        enableWindowDim = true,
        summary = "填写邮箱验证码后设置新密码。",
        summaryColor = MiuixTheme.colorScheme.onSurfaceSecondary,
        onDismissRequest = {
            if (busy) return@SuperDialog
            show.value = false
            onDismiss()
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = emailText,
                onValueChange = {
                    emailText = it
                    errorText = null
                    messageText = null
                },
                label = "邮箱",
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
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = passwordText,
                onValueChange = {
                    passwordText = it
                    errorText = null
                    messageText = null
                },
                label = "新密码",
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !busy,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = passwordConfirmText,
                onValueChange = {
                    passwordConfirmText = it
                    errorText = null
                    messageText = null
                },
                label = "确认新密码",
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !busy,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
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
                                SupabaseAuthRepository.sendPasswordRecoveryEmail(emailText)
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
                    text = if (busy) "处理中..." else "确认重置",
                    enabled = !busy && session != null,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        if (busy || session == null) return@TextButton
                        if (passwordText != passwordConfirmText) {
                            errorText = "两次输入的密码不一致"
                            return@TextButton
                        }
                        busy = true
                        errorText = null
                        messageText = null
                        scope.launch {
                            try {
                                SupabaseAuthRepository.resetPasswordWithRecoveryOtp(
                                    context = context,
                                    email = emailText,
                                    token = tokenText,
                                    newPassword = passwordText
                                )
                                show.value = false
                                onDismiss()
                                signOut(context)
                            } catch (e: SupabaseApiException) {
                                errorText = e.message
                            } catch (e: Exception) {
                                errorText = e.message ?: "重置密码失败"
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
