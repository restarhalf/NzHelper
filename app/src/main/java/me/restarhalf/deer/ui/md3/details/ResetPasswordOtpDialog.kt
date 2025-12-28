package me.restarhalf.deer.ui.md3.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository.signOut

@Composable
fun ResetPasswordOtpDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

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

    AlertDialog(
        onDismissRequest = {
            if (busy) return@AlertDialog
            onDismiss()
        },
        title = { Text("重置密码") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = emailText,
                    onValueChange = {
                        emailText = it
                        errorText = null
                        messageText = null
                    },
                    label = { Text("邮箱") },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = tokenText,
                    onValueChange = {
                        tokenText = it
                        errorText = null
                        messageText = null
                    },
                    label = { Text("验证码") },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = {
                        passwordText = it
                        errorText = null
                        messageText = null
                    },
                    label = { Text("新密码") },
                    singleLine = true,
                    enabled = !busy,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = passwordConfirmText,
                    onValueChange = {
                        passwordConfirmText = it
                        errorText = null
                        messageText = null
                    },
                    label = { Text("确认新密码") },
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    enabled = !busy && session != null,
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
                    }
                ) {
                    Text(if (busy) "发送中..." else "发送验证码")
                }
                TextButton(
                    enabled = !busy && session != null,
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
                                signOut(context)
                                onDismiss()
                            } catch (e: SupabaseApiException) {
                                errorText = e.message
                            } catch (e: Exception) {
                                errorText = e.message ?: "重置密码失败"
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) {
                    Text(if (busy) "处理中..." else "确认重置")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    if (busy) return@TextButton
                    onDismiss()
                }
            ) {
                Text("取消")
            }
        }
    )
}
