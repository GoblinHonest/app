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

package com.stark.miuix.ui.components

import com.stark.miuix.ui.theme.DesignTokens
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Shimmer 骨架屏加载效果
 *
 * 在数据加载时展示闪烁占位符，提升视觉体验。
 * 使用线性渐变动画模拟真实内容形状。
 *
 * @param columns 列数
 * @param itemCount 占位卡片数量
 */
@Composable
fun ShimmerVideoGrid(
    columns: Int = 3,
    itemCount: Int = 9
) {
    val transition = rememberInfiniteTransition()
    val shimmerOffset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 400f, 300f)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(itemCount) {
            ShimmerVideoCardPlaceholder(shimmerBrush)
        }
    }
}

/**
 * 单个视频卡片的 Shimmer 占位
 */
@Composable
private fun ShimmerVideoCardPlaceholder(brush: Brush) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 封面占位 — 2:3 海报比例，与 VideoCard 一致
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(DesignTokens.radiusCard))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}
