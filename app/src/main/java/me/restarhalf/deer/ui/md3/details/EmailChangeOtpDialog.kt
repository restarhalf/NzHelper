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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.restarhalf.deer.data.supabase.SupabaseApiException
import me.restarhalf.deer.data.supabase.SupabaseAuthRepository

@Composable
fun EmailChangeOtpDialog(
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
    var newEmailText by remember { mutableStateOf("") }
    var tokenText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            if (busy) return@AlertDialog
            onDismiss()
        },
        title = { Text("更改邮箱") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newEmailText,
                    onValueChange = {
                        newEmailText = it
                        errorText = null
                        messageText = null
                    },
                    label = { Text("新邮箱") },
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
                    }
                ) {
                    Text(if (busy) "发送中..." else "发送验证码")
                }
                TextButton(
                    enabled = !busy && session != null,
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
                                onDismiss()
                            } catch (e: SupabaseApiException) {
                                errorText = e.message
                            } catch (e: Exception) {
                                errorText = e.message ?: "更改邮箱失败"
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) {
                    Text(if (busy) "处理中..." else "确认更改")
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
