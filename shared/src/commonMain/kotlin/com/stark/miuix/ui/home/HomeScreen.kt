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

package com.stark.miuix.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoCard
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 首页 — 视频推荐与源概览
 *
 * 使用单一 [LazyVerticalGrid] 承载所有内容，
 * 避免 LazyVerticalGrid 嵌套在 LazyColumn 中导致的滚动冲突。
 * 头部信息区域使用 [GridItemSpan] 占满整行。
 */
@Composable
fun HomeScreen(
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    onNavigateToSearch: () -> Unit,
    onNavigateToCategory: (String, String) -> Unit,
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateToSourceManage: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember(videoRepository, sourceRepository, coroutineScope) {
        HomeViewModel(videoRepository, sourceRepository, coroutineScope)
    }
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val sources by sourceRepository.sources.collectAsState()

    // 监听源数量变化（非引用变化），避免无限循环
    val sourceCount = sources.size
    LaunchedEffect(sourceCount) {
        viewModel.loadVideos()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "视频聚合",
            actions = {
                if (isRefreshing) {
                    Text(
                        text = "刷新中...",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Text("刷新", style = MiuixTheme.textStyles.body2)
                }
                IconButton(onClick = onNavigateToSearch) {
                    Text("搜索", style = MiuixTheme.textStyles.body2)
                }
                IconButton(onClick = onNavigateToSourceManage) {
                    Text("源", style = MiuixTheme.textStyles.body2)
                }
            }
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                ShimmerVideoGrid()
            }

                is HomeUiState.Success -> {
                    if (state.sources.isEmpty()) {
                        EmptySourceHint(onNavigateToSourceManage)
                    } else {
                        HomeContentGrid(
                            state = state,
                            onVideoClick = { video ->
                                onNavigateToDetail(
                                    video.sourceName, video.url, video.title, video.cover
                                )
                            },
                            onNavigateToSourceManage = onNavigateToSourceManage,
                            onNavigateToCategory = onNavigateToCategory
                        )
                    }
                }

                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "加载失败",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "重试",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .clickable { viewModel.loadVideos() }
                            )
                        }
                    }
                }
            }
        }
    }

/**
 * 无视频源时的空状态引导
 */
@Composable
private fun EmptySourceHint(onNavigateToSourceManage: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "暂无视频源",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface
            )
            Text(
                text = "请先导入视频源",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Card(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { onNavigateToSourceManage() },
                cornerRadius = 12.dp
            ) {
                Text(
                    text = "导入视频源",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 首页内容网格
 *
 * 使用单一 LazyVerticalGrid 承载源状态卡片 + 视频卡片，
 * 头部信息通过 [GridItemSpan] maxLineSpan 占满整行。
 */
@Composable
private fun HomeContentGrid(
    state: HomeUiState.Success,
    onVideoClick: (com.stark.miuix.data.model.SearchResult) -> Unit,
    onNavigateToSourceManage: () -> Unit,
    onNavigateToCategory: (String, String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 源状态概览（占满整行）
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "已启用 ${state.sources.size} 个视频源",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "管理 >",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToSourceManage() }
                    )
                }
            }
        }

        // 源分类快捷入口（占满整行，水平滚动）
        if (state.sources.size > 1) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.sources.size) { index ->
                        val source = state.sources[index]
                        Card(
                            modifier = Modifier.clickable {
                                onNavigateToCategory(source.sourceName, "")
                            },
                            cornerRadius = 10.dp
                        ) {
                            Text(
                                text = source.sourceName,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 推荐视频标题（占满整行）
        if (state.videos.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "推荐视频",
                    style = MiuixTheme.textStyles.headline1,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // 视频卡片（自适应列数）
        items(
            items = state.videos,
            key = { "${it.sourceName}:${it.url}" }
        ) { video ->
            VideoCard(
                searchResult = video,
                onClick = { onVideoClick(video) }
            )
        }
    }
}
