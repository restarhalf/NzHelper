package me.restarhalf.deer.ui.dialogs


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.restarhalf.deer.data.SessionDraft
import me.restarhalf.deer.ui.components.TwoTextButtonsRow
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.CheckboxLocation
import top.yukonga.miuix.kmp.extra.SuperCheckbox
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.extra.WindowDropdown
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun DetailsDialog(
    show: Boolean,
    draft: SessionDraft,
    onDraftChange: (SessionDraft) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    val showDialog = remember { mutableStateOf(true) }
    val propItems = remember { listOf("手", "斐济杯", "小胶妻") }
    val moodItems = remember { listOf("平静", "愉悦", "兴奋", "疲惫", "这是最后一次！") }

    val propIndex = propItems.indexOf(draft.props).let { if (it >= 0) it else 0 }
    val moodIndex = moodItems.indexOf(draft.mood).let { if (it >= 0) it else 0 }
    WindowDialog(
        title = "填写本次信息",
        show = showDialog,
        onDismissRequest = {
            showDialog.value = false
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .overScrollVertical()
                .verticalScroll(
                    rememberScrollState(),
                    overscrollEffect = null
                )
        ) {
            Text(text = "备注（可选）", style = MiuixTheme.textStyles.subtitle)
            TextField(
                value = draft.remark,
                onValueChange = { onDraftChange(draft.copy(remark = it)) },
                label = "有什么想说的？",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "起飞地点（可选）", style = MiuixTheme.textStyles.subtitle)
            TextField(
                value = draft.location,
                onValueChange = { onDraftChange(draft.copy(location = it)) },
                label = "例如：卧室",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card {
                SuperCheckbox(
                    title = "是否观看小电影",
                    checked = draft.watchedMovie,
                    onCheckedChange = { onDraftChange(draft.copy(watchedMovie = it)) },
                    checkboxLocation = CheckboxLocation.End
                )
                SuperCheckbox(
                    title = "是否发射",
                    checked = draft.climax,
                    onCheckedChange = { onDraftChange(draft.copy(climax = it)) },
                    checkboxLocation = CheckboxLocation.End
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card {
                WindowDropdown(
                    title = "道具",
                    summary = draft.props,
                    items = propItems,
                    selectedIndex = propIndex,
                    onSelectedIndexChange = { idx ->
                        onDraftChange(draft.copy(props = propItems[idx]))
                    }
                )
                WindowDropdown(
                    title = "心情",
                    summary = draft.mood,
                    items = moodItems,
                    selectedIndex = moodIndex,
                    onSelectedIndexChange = { idx ->
                        onDraftChange(draft.copy(mood = moodItems[idx]))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "评分：${"%.1f".format(draft.rating)}",
                style = MiuixTheme.textStyles.subtitle
            )
            Slider(
                value = draft.rating,
                onValueChange = { onDraftChange(draft.copy(rating = it)) },
                valueRange = 0f..5f,
                steps = 25,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "0", style = MiuixTheme.textStyles.body2)
                Text(text = "5.0", style = MiuixTheme.textStyles.body2)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TwoTextButtonsRow(
                leftText = "取消",
                onLeftClick = {
                    showDialog.value = false
                    onDismiss()
                },
                rightText = "确认",
                onRightClick = {
                    showDialog.value = false
                    onConfirm()
                },
            )
        }
    }
}
