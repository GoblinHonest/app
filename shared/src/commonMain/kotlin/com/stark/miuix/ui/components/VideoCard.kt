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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.ui.theme.DesignTokens
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频卡片 — 视频 App 专业风格
 *
 * 设计参考 B站/YouTube 卡片：
 * - 16:10 圆角封面 + 底部渐变遮罩
 * - 来源标签（右上角半透明芯片）
 * - 标题两行、描述一行、紧凑间距
 */
@Composable
fun VideoCard(
    searchResult: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .clickable(onClick = onClick)
    ) {
        // 封面区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(DesignTokens.coverAspectRatio)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
        ) {
            if (searchResult.cover.isNotBlank()) {
                val painterResource = asyncPainterResource(searchResult.cover)
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
                    onFailure = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = searchResult.title.take(2),
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
            }

            // 底部渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )

            // 来源标签（右上角）
            if (searchResult.sourceName.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(DesignTokens.spacingSm)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(DesignTokens.radiusSm)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = searchResult.sourceName,
                        style = MiuixTheme.textStyles.footnote2,
                        color = Color.White
                    )
                }
            }
        }

        // 信息区域
        Column(
            modifier = Modifier.padding(
                horizontal = DesignTokens.spacingXs,
                vertical = DesignTokens.spacingSm
            )
        ) {
            Text(
                text = searchResult.title,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (searchResult.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
                Text(
                    text = searchResult.description,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
