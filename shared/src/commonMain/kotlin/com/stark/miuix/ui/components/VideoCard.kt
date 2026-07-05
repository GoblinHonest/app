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
 * 视频卡片 — 竖向海报风格（参考优酷/B站设计）
 *
 * 布局：
 * - 2:3 竖向海报封面（主流视频 App 标准比例）
 * - 左下角状态角标（热搜/更新中）
 * - 底部渐变叠层 + 集数信息
 * - 卡片下方显示标题
 *
 * 点击时有轻微缩放反馈（scale 0.96）。
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
                pressed = true
                onClick()
            }
    ) {
        // 封面区域（2:3 竖向海报比例）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
        ) {
            val coverUrl = searchResult.cover
            if (coverUrl.isNotBlank() &&
                (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))
            ) {
                val painterResource = asyncPainterResource(data = coverUrl)
                KamelImage(
                    resource = painterResource,
                    contentDescription = searchResult.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MiuixTheme.colorScheme.surfaceVariant)
                        )
                    },
                    onFailure = { exception ->
                        AppLogger.e("Image", "加载失败: $coverUrl", exception)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MiuixTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = searchResult.title.take(2),
                                style = MiuixTheme.textStyles.headline1,
                                color = MiuixTheme.colorScheme.outline
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MiuixTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = searchResult.title.take(2),
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
            }

            // 底部渐变 — 显示集数/描述
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // 左下角角标（更新状态）
            val badgeText = badge.ifBlank {
                if (searchResult.description.isNotBlank()) searchResult.description.take(8)
                else ""
            }
            if (badgeText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(
                            Color(0xFFE11D48).copy(alpha = 0.9f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MiuixTheme.textStyles.footnote2,
                        color = Color.White
                    )
                }
            }
        }

        // 标题
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
