package me.restarhalf.deer.ui.miuix.icons.custom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Stat: ImageVector = Builder(
    name = "Stat",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    // 1. 主体扇形 (左下方的大块)
    // 从 3点钟方向 (19, 13) 顺时针画到 12点钟方向 (11, 5)
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(11f, 13f) // 圆心
        lineTo(19f, 13f) // 边缘
        // 顺时针(true)，大圆(true)，270度
        arcTo(8f, 8f, 0f, true, true, 11f, 5f)
        close()
    }

    // 2. 分离的切片 (右上角的小块)
    // 从 12点钟方向 (14, 2) 顺时针画到 3点钟方向 (22, 10)
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(14f, 10f) // 新圆心
        lineTo(14f, 2f)  // 边缘
        // 顺时针(true)，小圆(false)，90度
        arcTo(8f, 8f, 0f, false, true, 22f, 10f)
        close()
    }
}.build() // <--- 关键：这里必须有 .build() 否则会报类型不匹配错误