package me.restarhalf.deer.ui.miuix.icons.custom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Rank: ImageVector = Builder(
    name = "Rank",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 第二名 (左)
        moveTo(6f, 21f)
        lineTo(6f, 13f)

        // 第一名 (中) - 突出显示，最高
        moveTo(12f, 21f)
        lineTo(12f, 5f)

        // 第三名 (右)
        moveTo(18f, 21f)
        lineTo(18f, 16f)

        // (可选) 如果想要更整体的感觉，可以将它们连在一个底座上，
        // 但 HyperOS 更倾向于这种独立悬浮的极简圆柱体。
    }
}.build()