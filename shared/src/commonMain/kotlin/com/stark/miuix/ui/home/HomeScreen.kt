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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoCard
import com.stark.miuix.ui.theme.DesignTokens
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 首页 — 视频推荐与发现
 *
 * 布局结构（从上到下）：
 * 1. 搜索入口栏（点击跳转搜索页）
 * 2. 源分类横滑标签
 * 3. 视频推荐网格（自适应列数，平板 3-4 列）
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
    val viewModel = remember(videoRepository, sourceRepository) {
        HomeViewModel(videoRepository, sourceRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val sources by sourceRepository.sources.collectAsState()

    val sourceCount = sources.size
    LaunchedEffect(sourceCount) {
        viewModel.loadVideos()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部搜索入口
        SearchEntryBar(
            isRefreshing = isRefreshing,
            onSearchClick = onNavigateToSearch,
            onRefreshClick = { viewModel.refresh() },
            onSourceClick = onNavigateToSourceManage
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
                        onNavigateToCategory = onNavigateToCategory,
                        onNavigateToSourceManage = onNavigateToSourceManage
                    )
                }
            }
            is HomeUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadVideos() }
                )
            }
        }
    }
}

/**
 * 搜索入口栏 — 模拟搜索框（点击跳转搜索页）
 */
@Composable
private fun SearchEntryBar(
    isRefreshing: Boolean,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSourceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
    ) {
        // 搜索框区域
        Box(
            modifier = Modifier
                .weight(1f)
                .height(DesignTokens.searchBarHeight)
                .clip(RoundedCornerShape(DesignTokens.radiusLg))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSearchClick)
                .padding(horizontal = DesignTokens.spacingLg),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "搜索视频...",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.outline
            )
        }

        // 刷新按钮
        Box(
            modifier = Modifier
                .height(DesignTokens.searchBarHeight)
                .width(DesignTokens.searchBarHeight)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onRefreshClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRefreshing) "..." else "刷",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.primary
            )
        }

        // 源管理按钮
        Box(
            modifier = Modifier
                .height(DesignTokens.searchBarHeight)
                .width(DesignTokens.searchBarHeight)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSourceClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "源",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 空状态引导
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
                style = MiuixTheme.textStyles.headline1,
                color = MiuixTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            Text(
                text = "导入视频源后即可浏览海量内容",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(DesignTokens.spacingXl))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusXl))
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable { onNavigateToSourceManage() }
                    .padding(horizontal = 32.dp, vertical = DesignTokens.spacingMd),
            ) {
                Text(
                    text = "导入视频源",
                    style = MiuixTheme.textStyles.body1,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

/**
 * 错误内容
 */
@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
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
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            Text(
                text = message,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusXl))
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 24.dp, vertical = DesignTokens.spacingSm)
            ) {
                Text(
                    text = "重试",
                    style = MiuixTheme.textStyles.body2,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

/**
 * 首页内容网格
 *
 * 头部：源分类横滑标签
 * 主体：视频卡片自适应网格（手机 2 列，平板自动增列）
 */
@Composable
private fun HomeContentGrid(
    state: HomeUiState.Success,
    onVideoClick: (SearchResult) -> Unit,
    onNavigateToCategory: (String, String) -> Unit,
    onNavigateToSourceManage: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = DesignTokens.gridMinWidth),
        contentPadding = PaddingValues(
            horizontal = DesignTokens.screenPadding,
            vertical = DesignTokens.spacingSm
        ),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.cardGap),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.cardGap)
    ) {
        // 源分类标签（占满整行）
        if (state.sources.size > 1) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
                    modifier = Modifier.padding(bottom = DesignTokens.spacingSm)
                ) {
                    items(state.sources.size) { index ->
                        val source = state.sources[index]
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(DesignTokens.radiusXl))
                                .background(MiuixTheme.colorScheme.surfaceVariant)
                                .clickable { onNavigateToCategory(source.sourceName, "") }
                                .padding(horizontal = DesignTokens.spacingLg, vertical = DesignTokens.spacingSm)
                        ) {
                            Text(
                                text = source.sourceName,
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 区域标题
        if (state.videos.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "推荐",
                    style = MiuixTheme.textStyles.headline1,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = DesignTokens.spacingXs)
                )
            }
        }

        // 视频卡片
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
