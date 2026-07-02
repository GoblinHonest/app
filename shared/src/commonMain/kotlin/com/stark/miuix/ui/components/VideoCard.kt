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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SquircleShape

/**
 * 视频卡片组件
 *
 * 以 Squircle 超椭圆圆角卡片形式展示视频信息，包含：
 * - 封面图片（16:9 比例，带毛玻璃占位背景）
 * - 标题（单行截断）
 * - 描述（可选，单行截断）
 *
 * 视觉风格遵循 HyperOS 设计语言，使用 Miuix 的 SquircleShape 实现
 * 小米标志性的平滑圆角效果。
 *
 * @param searchResult 搜索结果数据
 * @param onClick 点击回调，传递详情页 URL
 * @param modifier Modifier 修饰符
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
        shape = SquircleShape(16.dp)
    ) {
        Column {
            // 封面图片区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(SquircleShape(16.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant)
            ) {
                if (searchResult.cover.isNotBlank()) {
                    val painterResource = asyncPainterResource(searchResult.cover)
                    KamelImage(
                        resource = painterResource,
                        contentDescription = searchResult.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onFailure = { /* 加载失败时显示占位背景 */ }
                    )
                }
            }

            // 视频信息区域
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // 标题
                Text(
                    text = searchResult.title,
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 描述（如果有）
                if (searchResult.description.isNotBlank()) {
                    Text(
                        text = searchResult.description,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // 来源标签
                if (searchResult.sourceName.isNotBlank()) {
                    Text(
                        text = searchResult.sourceName,
                        style = MiuixTheme.textStyles.caption,
                        color = MiuixTheme.colorScheme.outline,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
