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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频卡片组件
 *
 * 以圆角卡片形式展示视频信息：封面 + 标题 + 来源标签。
 * 封面区域具备加载中占位背景和加载失败兜底显示。
 */
@Composable
fun VideoCard(
    searchResult: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 16.dp
    ) {
        Column {
            // 封面图片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
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
                            Text(
                                text = "封面加载失败",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.outline
                            )
                        }
                    )
                } else {
                    Text(
                        text = searchResult.title.take(1),
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
            }

            // 视频信息
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = searchResult.title,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (searchResult.description.isNotBlank()) {
                    Text(
                        text = searchResult.description,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (searchResult.sourceName.isNotBlank()) {
                    Text(
                        text = searchResult.sourceName,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
