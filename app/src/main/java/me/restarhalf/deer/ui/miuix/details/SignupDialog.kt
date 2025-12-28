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
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SignupDialog(
    show: MutableState<Boolean>,
    onConfirm: (email: String, password: String, nickname: String) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    if (!show.value) return
    SuperDialog(
        title = "注册",
        titleColor = MiuixTheme.colorScheme.onSurface,
        show = show,
        enableWindowDim = true,
        summary = "注册账号。",
        summaryColor = MiuixTheme.colorScheme.onSurfaceSecondary,
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            var nicknameText by remember { mutableStateOf("") }
            TextField(
                value = nicknameText,
                onValueChange = { nicknameText = it },
                label = "昵称",
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            var accText by remember { mutableStateOf("") }
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
            Spacer(modifier = Modifier.height(12.dp))
            var scrTextConfirm by remember { mutableStateOf("") }
            TextField(
                value = scrTextConfirm,
                onValueChange = { scrTextConfirm = it },
                label = "确认密码",
                useLabelAsPlaceholder = true,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (scrText != scrTextConfirm) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "两次输入的密码不一致",
                    color = MiuixTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    text = "取消",
                    colors = ButtonDefaults.textButtonColors(),
                    onClick = {
                        onCancel()
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    text = "注册",
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        if (scrText != scrTextConfirm) return@TextButton
                        onDismiss()
                        onConfirm(accText, scrText, nicknameText)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}
