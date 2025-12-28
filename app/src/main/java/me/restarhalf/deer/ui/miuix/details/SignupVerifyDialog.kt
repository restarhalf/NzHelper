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
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SignupVerifyDialog(
    show: MutableState<Boolean>,
    initialEmail: String = "",
    busy: Boolean = false,
    messageText: String? = null,
    errorText: String? = null,
    onResend: (email: String) -> Unit,
    onVerify: (email: String, token: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!show.value) return

    var emailText by remember(initialEmail) { mutableStateOf(initialEmail) }
    var tokenText by remember(initialEmail) { mutableStateOf("") }

    SuperDialog(
        title = "邮箱验证",
        titleColor = MiuixTheme.colorScheme.onSurface,
        show = show,
        enableWindowDim = true,
        summary = "请输入邮箱收到的验证码。",
        summaryColor = MiuixTheme.colorScheme.onSurfaceSecondary,
        onDismissRequest = {
            if (busy) return@SuperDialog
            onDismiss()
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = emailText,
                onValueChange = { emailText = it },
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
                onValueChange = { tokenText = it },
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
                    text = messageText,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
            }
            if (errorText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText,
                    color = MiuixTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    text = "发送验证码",
                    colors = ButtonDefaults.textButtonColors(),
                    enabled = !busy,
                    onClick = {
                        onResend(emailText)
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    text = "验证",
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    enabled = !busy,
                    onClick = {
                        onVerify(emailText, tokenText)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
