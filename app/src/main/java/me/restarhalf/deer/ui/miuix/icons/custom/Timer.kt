package me.restarhalf.deer.ui.miuix.icons.custom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Timer: ImageVector = Builder(
    name = "Timer",
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
        // 1. 画秒表外圈 (中心 y=13, 半径=9)
        // 起点在顶部 (12, 4)
        moveTo(12f, 4f)
        // 右半圆: 顺时针(true), 大于半圆(false - 分两段画更稳)
        // arcTo参数: rx, ry, rotation, isMoreThanHalf, isPositiveArc(方向), x, y

        // 画右半边到 (12, 22)
        arcTo(9f, 9f, 0f, false, true, 12f, 22f)
        // 画左半边回到 (12, 4)
        arcTo(9f, 9f, 0f, false, true, 12f, 4f)

        // 2. 顶部按钮
        moveTo(12f, 4f)
        lineTo(12f, 2f)

        // 3. 指针 (分针短，时针长)
        // 中心点 (12, 13)
        moveTo(12f, 13f)
        lineTo(12f, 9f)   // 向上指

        moveTo(12f, 13f)
        lineTo(14.5f, 15.5f) // 向右下指
    }
}.build()