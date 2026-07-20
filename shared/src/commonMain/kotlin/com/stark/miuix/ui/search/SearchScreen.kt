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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.unit.dp
import com.stark.miuix.di.AppContainer
import com.stark.miuix.ui.components.EmptyStateView
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoGrid
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.ui.icons.IconBack
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
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
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel = AppContainer.searchViewModel
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    val initialQuery = when (val s = uiState) {
        is SearchUiState.Success -> s.keyword
        is SearchUiState.Error -> s.keyword
        else -> ""
    }
    var query by remember { mutableStateOf(initialQuery) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏 — 与首页一样的顶部渐变 + statusBarsPadding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            com.stark.miuix.ui.theme.DesignTokens.brandBlue.copy(alpha = 0.10f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = DesignTokens.screenPadding,
                    vertical = DesignTokens.spacingMd
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            IconButton(onClick = onNavigateBack) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconBack),
                    contentDescription = "返回",
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                    modifier = androidx.compose.ui.Modifier.padding(4.dp)
                )
            }
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.onQueryChanged(it)
                },
                modifier = Modifier.weight(1f),
                label = "搜索动漫、番剧..."
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
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
        } // Box gradient

        // 联想词下拉
        if (suggestions.isNotEmpty() && uiState is SearchUiState.Idle) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.screenPadding)
                    .clip(RoundedCornerShape(DesignTokens.radiusMd))
                    .background(MiuixTheme.colorScheme.surface)
            ) {
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                query = suggestion
                                viewModel.search(suggestion)
                            }
                            .padding(horizontal = DesignTokens.spacingMd, vertical = 10.dp)
                    ) {
                        Text(
                            text = suggestion,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
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
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(DesignTokens.radiusXl))
                                        .background(MiuixTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            query = keyword
                                            viewModel.search(keyword)
                                        }
                                        .padding(start = DesignTokens.spacingMd, end = 4.dp, top = DesignTokens.spacingSm, bottom = DesignTokens.spacingSm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = keyword,
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                    // × 删除单条历史
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { viewModel.removeHistoryItem(keyword) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "×",
                                            style = MiuixTheme.textStyles.footnote1,
                                            color = MiuixTheme.colorScheme.outline
                                        )
                                    }
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
                    EmptyStateView(
                        title = "未找到相关内容",
                        message = "试试换个关键词",
                        actionText = if (history.isNotEmpty()) "用「${history.first()}」重新搜" else "",
                        onAction = { if (history.isNotEmpty()) { query = history.first(); viewModel.search(history.first()) } }
                    )
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
                            modifier = Modifier.weight(1f)
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
