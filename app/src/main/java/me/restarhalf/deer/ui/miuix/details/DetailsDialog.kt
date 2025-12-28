package me.restarhalf.deer.ui.miuix.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.CheckboxLocation
import top.yukonga.miuix.kmp.extra.SuperCheckbox
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun DetailsDialog(
    show: Boolean,
    remark: String,
    onRemarkChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    watchedMovie: Boolean,
    onWatchedMovieChange: (Boolean) -> Unit,
    climax: Boolean,
    onClimaxChange: (Boolean) -> Unit,
    props: String,
    onPropsChange: (String) -> Unit,
    rating: Float,
    onRatingChange: (Float) -> Unit,
    mood: String,
    onMoodChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    val showDialog = remember { mutableStateOf(true) }
    val propItems = remember { listOf("手", "斐济杯", "小胶妻") }
    val moodItems = remember { listOf("平静", "愉悦", "兴奋", "疲惫", "这是最后一次！") }

    val propIndex = propItems.indexOf(props).let { if (it >= 0) it else 0 }
    val moodIndex = moodItems.indexOf(mood).let { if (it >= 0) it else 0 }
    SuperDialog(
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
                value = remark,
                onValueChange = onRemarkChange,
                label = "有什么想说的？",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "起飞地点（可选）", style = MiuixTheme.textStyles.subtitle)
            TextField(
                value = location,
                onValueChange = onLocationChange,
                label = "例如：卧室",
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card {
                SuperCheckbox(
                    title = "是否观看小电影",
                    checked = watchedMovie,
                    onCheckedChange = onWatchedMovieChange,
                    checkboxLocation = CheckboxLocation.Right
                )
                SuperCheckbox(
                    title = "是否发射",
                    checked = climax,
                    onCheckedChange = onClimaxChange,
                    checkboxLocation = CheckboxLocation.Right
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card {
                SuperDropdown(
                    title = "道具",
                    summary = props,
                    items = propItems,
                    selectedIndex = propIndex,
                    onSelectedIndexChange = { idx ->
                        onPropsChange(propItems[idx])
                    }
                )
                SuperDropdown(
                    title = "心情",
                    summary = mood,
                    items = moodItems,
                    selectedIndex = moodIndex,
                    onSelectedIndexChange = { idx ->
                        onMoodChange(moodItems[idx])
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "评分：${"%.1f".format(rating)}",
                style = MiuixTheme.textStyles.subtitle
            )
            Slider(
                value = rating,
                onValueChange = onRatingChange,
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

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    text = "取消",
                    onClick = {
                        showDialog.value = false
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    text = "确认",
                    onClick = {
                        showDialog.value = false
                        onConfirm()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsDialogPreview() {
    DetailsDialog(
        show = true,
        remark = "",
        onRemarkChange = {},
        location = "",
        onLocationChange = {},
        watchedMovie = false,
        onWatchedMovieChange = {},
        climax = false,
        onClimaxChange = {},
        props = "手",
        onPropsChange = {},
        rating = 3.5f,
        onRatingChange = {},
        mood = "愉悦",
        onMoodChange = {},
        onConfirm = {},
        onDismiss = {}
    )
}