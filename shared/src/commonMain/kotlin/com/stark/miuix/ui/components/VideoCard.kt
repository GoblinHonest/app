/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.AppLogger
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频卡片 — 对标优酷 App 设计
 *
 * 封面：2:3 竖向海报比例，左上角状态芯片（有更新/热搜），右上角标签（独播）
 * 封面底部：白色小字「更新至X话」
 * 卡片下方：进度文字 + 描述剪辑
 * 点击动画：0.96 缩放
 */
@Composable
fun VideoCard(
    searchResult: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String = ""
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "card_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                pressed = false
                onClick()
            }
    ) {
        // —— 封面 ——
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(DesignTokens.coverAspectRatio)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
        ) {
            val coverUrl = searchResult.cover
            if (coverUrl.isNotBlank() &&
                (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))
            ) {
                KamelImage(
                    resource = { asyncPainterResource(data = coverUrl) },
                    contentDescription = searchResult.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        Box(modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surfaceVariant))
                    },
                    onFailure = { e ->
                        AppLogger.e("Image", "加载失败: $coverUrl", e)
                        Box(
                            modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(searchResult.title.take(2), style = MiuixTheme.textStyles.headline1, color = MiuixTheme.colorScheme.outline)
                        }
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(searchResult.title.take(2), style = MiuixTheme.textStyles.headline1, color = MiuixTheme.colorScheme.outline)
                }
            }

            // 封面底部渐变
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )

            // 左上角状态芯片（有更新 / 热搜榜）
            val statusBadge = badge.ifBlank {
                if (searchResult.description.contains("更新")) "有更新"
                else ""
            }
            if (statusBadge.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .background(
                            Color(0xFFE11D48).copy(alpha = 0.92f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(statusBadge, style = MiuixTheme.textStyles.footnote2, color = Color.White)
                }
            }

            // 底部更新信息
            if (searchResult.description.isNotBlank()) {
                Text(
                    text = searchResult.description.take(8),
                    style = MiuixTheme.textStyles.footnote2,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 4.dp)
                )
            }
        }

        // —— 封面下标题 ——
        Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
        Text(
            text = searchResult.title,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
