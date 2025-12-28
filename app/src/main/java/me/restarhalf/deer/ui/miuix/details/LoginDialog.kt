package me.restarhalf.deer.ui.miuix.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LoginDialog(
    show: MutableState<Boolean>,
    onConfirm: (email: String, password: String) -> Unit,
    onSendOtp: (email: String) -> Unit,
    onVerifyOtp: (email: String, token: String) -> Unit,
    onDismiss: () -> Unit,
    onSignup: () -> Unit
) {
    if (!show.value) return
    SuperDialog(
        title = "登录",
        titleColor = MiuixTheme.colorScheme.onSurface,
        show = show,
        enableWindowDim = true,
        summary = "登录以使用排行榜。",
        summaryColor = MiuixTheme.colorScheme.onSurfaceSecondary,
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            var accText by remember { mutableStateOf("") }
            var tokenText by remember { mutableStateOf("") }
            var useOtp by remember { mutableStateOf(false) }
            TextField(
                value = accText,
                onValueChange = { accText = it },
                label = "邮箱",
                useLabelAsPlaceholder = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            var scrText by remember { mutableStateOf("") }
            if (useOtp) {
                TextField(
                    value = tokenText,
                    onValueChange = { tokenText = it },
                    label = "验证码",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                TextField(
                    value = scrText,
                    onValueChange = { scrText = it },
                    label = "密码",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    text = if (useOtp) "密码登录" else "验证码登录",
                    colors = ButtonDefaults.textButtonColors(),
                    onClick = {
                        useOtp = !useOtp
                        if (useOtp) {
                            scrText = ""
                        } else {
                            tokenText = ""
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    text = "注册",
                    colors = ButtonDefaults.textButtonColors(),
                    onClick = {
                        onDismiss()
                        onSignup()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (useOtp) {
                    TextButton(
                        text = "发送验证码",
                        colors = ButtonDefaults.textButtonColors(),
                        onClick = {
                            onSendOtp(accText)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        text = "登录",
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismiss()
                            onVerifyOtp(accText, tokenText)
                        }
                    )
                } else {
                    TextButton(
                        text = "登录",
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onDismiss()
                            onConfirm(accText, scrText)
                        }
                    )
                }
            }
        }
    }
}