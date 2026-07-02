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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.di.AppContainer
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoGrid
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 搜索页
 *
 * 提供搜索栏、搜索历史（标签云）、搜索结果网格。
 * 搜索时使用 Shimmer 骨架屏替代普通 loading。
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
        TopAppBar(title = "搜索")

        // 搜索栏 + 搜索按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.onQueryChanged(it)
                },
                modifier = Modifier.weight(1f),
                label = "搜索视频..."
            )
            Card(
                modifier = Modifier.clickable {
                    if (query.isNotBlank()) viewModel.search(query)
                },
                cornerRadius = 10.dp
            ) {
                Text(
                    text = "搜索",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.primary
                )
            }
        }

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                if (history.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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

                        FlowRow(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            history.forEach { keyword ->
                                Card(
                                    modifier = Modifier.clickable {
                                        query = keyword
                                        viewModel.search(keyword)
                                    },
                                    cornerRadius = 8.dp
                                ) {
                                    Text(
                                        text = keyword,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
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
                                text = "未找到 \"${state.keyword}\" 相关内容",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                            Text(
                                text = "试试换个关键词，或检查视频源是否可用",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (history.isNotEmpty()) {
                                Text(
                                    text = "试试这些:",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.outline,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                FlowRow(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    history.take(5).forEach { keyword ->
                                        Card(
                                            modifier = Modifier.clickable {
                                                query = keyword
                                                viewModel.search(keyword)
                                            },
                                            cornerRadius = 8.dp
                                        ) {
                                            Text(
                                                text = keyword,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
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
