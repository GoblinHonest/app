/*
 * Copyright 2024 Stark Industries
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stark.miuix.data.danmaku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

/**
 * 弹幕渲染配置
 */
data class DanmakuConfig(
    val enabled: Boolean = true,
    val opacity: Float = 0.8f,
    val speed: Float = 1f,
    val fontSize: Float = 14f,
    val maxLines: Int = 8,
    val showTop: Boolean = true,
    val showBottom: Boolean = true,
    val showScroll: Boolean = true
)

/**
 * 弹幕渲染层
 *
 * 使用 Compose Canvas + TextMeasurer 绘制滚动/顶部/底部弹幕。
 * 滚动弹幕从右向左移动，顶部/底部弹幕居中静止。
 *
 * @param entries 弹幕数据列表
 * @param positionMs 当前播放位置（毫秒）
 * @param config 渲染配置
 */
@Composable
fun DanmakuOverlay(
    entries: List<DanmakuEntry>,
    positionMs: Long,
    config: DanmakuConfig = DanmakuConfig()
) {
    if (!config.enabled || entries.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val fontSizePx = with(density) { config.fontSize.sp.toPx() }

    val visibleEntries = remember(entries, positionMs) {
        entries.filter { entry ->
            val diff = positionMs - entry.timeMs
            when (entry.mode) {
                1 -> diff in 0..8000
                4, 5 -> diff in 0..5000
                else -> false
            }
        }.take(50)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val lineHeight = fontSizePx * 1.4f

        visibleEntries.forEachIndexed { index, entry ->
            val diff = positionMs - entry.timeMs
            val alpha = config.opacity.coerceIn(0f, 1f)
            val color = Color(entry.color or 0xFF000000.toInt()).copy(alpha = alpha)
            val lineIndex = index % config.maxLines
            val y = lineHeight * (lineIndex + 1)

            val style = TextStyle(
                color = color,
                fontSize = config.fontSize.sp
            )

            when {
                entry.mode == 1 && config.showScroll -> {
                    val progress = diff / 8000f * config.speed
                    val x = width - progress * (width + 200f)
                    if (x > -200f) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = entry.text,
                            topLeft = Offset(x, y),
                            style = style
                        )
                    }
                }
                entry.mode == 5 && config.showTop -> {
                    val layoutResult = textMeasurer.measure(entry.text, style)
                    val textWidth = layoutResult.size.width.toFloat()
                    drawText(
                        textMeasurer = textMeasurer,
                        text = entry.text,
                        topLeft = Offset((width - textWidth) / 2f, y),
                        style = style
                    )
                }
                entry.mode == 4 && config.showBottom -> {
                    val bottomY = height - lineHeight * (lineIndex + 1)
                    val layoutResult = textMeasurer.measure(entry.text, style)
                    val textWidth = layoutResult.size.width.toFloat()
                    drawText(
                        textMeasurer = textMeasurer,
                        text = entry.text,
                        topLeft = Offset((width - textWidth) / 2f, bottomY),
                        style = style
                    )
                }
            }
        }
    }
}
