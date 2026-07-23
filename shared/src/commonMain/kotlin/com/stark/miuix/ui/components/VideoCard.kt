/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.remember
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
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.theme.DesignTokens
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频海报卡片 v2
 *
 * 2:3 竖向海报，底部 40% 渐变遮罩 + 标题/源名，
 * 左上角状态芯片，按压 0.96 缩放反馈（150ms ease-out）。
 */
@Composable
fun VideoCard(
    searchResult: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = DesignTokens.animFast),
        label = "card_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(DesignTokens.coverAspectRatio)
                .clip(RoundedCornerShape(DesignTokens.radiusCard))
                .background(MiuixTheme.colorScheme.surfaceVariant)
        ) {
            val coverUrl = searchResult.cover
            if (coverUrl.isNotBlank() &&
                (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))
            ) {
                val ctx = LocalPlatformContext.current
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(coverUrl).build(),
                    contentDescription = searchResult.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        searchResult.title.take(2),
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(DesignTokens.posterGradientStart, AppColors.posterGradientEnd())
                        )
                    )
            )

            val statusBadge = badge.ifBlank {
                when {
                    searchResult.description.contains("全") -> "已完结"
                    searchResult.description.contains("更新") -> "有更新"
                    else -> ""
                }
            }
            if (statusBadge.isNotBlank()) {
                val badgeColor = if (statusBadge == "已完结") DesignTokens.badgeGreen else DesignTokens.badgeRed
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(DesignTokens.spacingXs)
                        .background(badgeColor.copy(alpha = 0.9f), RoundedCornerShape(DesignTokens.radiusXs))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(statusBadge, style = MiuixTheme.textStyles.footnote2, color = Color.White)
                }
            }

            if (searchResult.description.isNotBlank()) {
                Text(
                    text = searchResult.description.take(10),
                    style = MiuixTheme.textStyles.footnote2,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
        Text(
            text = searchResult.title.ifBlank { searchResult.sourceName },
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (searchResult.sourceName.isNotBlank()) {
            Text(
                text = searchResult.sourceName,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
