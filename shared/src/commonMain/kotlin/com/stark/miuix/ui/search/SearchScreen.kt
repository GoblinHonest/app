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

package com.stark.miuix.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stark.miuix.di.AppContainer
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoGrid
import com.stark.miuix.ui.theme.DesignTokens
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 搜索页 — 全屏搜索体验
 *
 * 顶部真实搜索框（可输入）+ 搜索按钮，
 * 下方根据状态展示：历史标签 / 骨架屏 / 结果网格 / 空结果引导。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (String, String, String, String) -> Unit
) {
    val viewModel = AppContainer.searchViewModel
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()

    val initialQuery = when (val s = uiState) {
        is SearchUiState.Success -> s.keyword
        is SearchUiState.Error -> s.keyword
        else -> ""
    }
    var query by remember { mutableStateOf(initialQuery) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignTokens.screenPadding,
                    vertical = DesignTokens.spacingMd
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.onQueryChanged(it)
                },
                modifier = Modifier.weight(1f),
                label = "搜索视频、演员、导演..."
            )
            Box(
                modifier = Modifier
                    .height(DesignTokens.searchBarHeight)
                    .clip(RoundedCornerShape(DesignTokens.radiusMd))
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable {
                        if (query.isNotBlank()) viewModel.search(query)
                    }
                    .padding(horizontal = DesignTokens.spacingLg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "搜索",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onPrimary
                )
            }
        }

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                if (history.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.screenPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "搜索历史",
                                style = MiuixTheme.textStyles.body1,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "清除",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.outline,
                                modifier = Modifier.clickable { viewModel.clearHistory() }
                            )
                        }
                        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
                        ) {
                            history.forEach { keyword ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(DesignTokens.radiusXl))
                                        .background(MiuixTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            query = keyword
                                            viewModel.search(keyword)
                                        }
                                        .padding(horizontal = DesignTokens.spacingMd, vertical = DesignTokens.spacingSm)
                                ) {
                                    Text(
                                        text = keyword,
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is SearchUiState.Searching -> {
                ShimmerVideoGrid()
            }

            is SearchUiState.Success -> {
                if (state.results.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = "未找到相关内容",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                            Text(
                                text = "试试换个关键词",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.outline
                            )
                            if (history.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
                                    verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
                                ) {
                                    history.take(5).forEach { keyword ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(DesignTokens.radiusXl))
                                                .background(MiuixTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    query = keyword
                                                    viewModel.search(keyword)
                                                }
                                                .padding(horizontal = DesignTokens.spacingMd, vertical = DesignTokens.spacingSm)
                                        ) {
                                            Text(
                                                text = keyword,
                                                style = MiuixTheme.textStyles.footnote1,
                                                color = MiuixTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val sourceCount = state.results.map { it.sourceName }.distinct().size
                        Text(
                            text = "从 ${sourceCount} 个源找到 ${state.results.size} 条结果",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.outline,
                            modifier = Modifier.padding(
                                horizontal = DesignTokens.screenPadding,
                                vertical = DesignTokens.spacingSm
                            )
                        )
                        VideoGrid(
                            videos = state.results,
                            onVideoClick = { video ->
                                onNavigateToDetail(
                                    video.sourceName, video.url, video.title, video.cover
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            is SearchUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.search(query) }
                )
            }
        }
    }
}
