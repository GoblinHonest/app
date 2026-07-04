/*
 * Copyright 2024 Stark Industries
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.VideoCard
import com.stark.miuix.ui.theme.DesignTokens
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
        SearchEntryBar(
            isRefreshing = isRefreshing,
            onSearchClick = onNavigateToSearch,
            onRefreshClick = { viewModel.refresh() },
            onSourceClick = onNavigateToSourceManage
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Success -> {
                if (state.sources.isEmpty()) {
                    EmptySourceHint(onNavigateToSourceManage)
                } else {
                    HomeContent(
                        state = state,
                        onVideoClick = { video ->
                            onNavigateToDetail(video.sourceName, video.url, video.title, video.cover)
                        },
                        onNavigateToCategory = onNavigateToCategory
                    )
                }
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "加载失败", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(DesignTokens.radiusXl))
                                .background(MiuixTheme.colorScheme.primary)
                                .clickable { viewModel.loadVideos() }
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(text = "重试", style = MiuixTheme.textStyles.body2, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }
        }
    }
}

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
            Text(text = "搜索视频...", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.outline)
        }
        Box(
            modifier = Modifier
                .height(DesignTokens.searchBarHeight)
                .width(DesignTokens.searchBarHeight)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onRefreshClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isRefreshing) "..." else "刷", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
        }
        Box(
            modifier = Modifier
                .height(DesignTokens.searchBarHeight)
                .width(DesignTokens.searchBarHeight)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSourceClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "源", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun EmptySourceHint(onNavigateToSourceManage: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "暂无视频源", style = MiuixTheme.textStyles.headline1, color = MiuixTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "导入视频源后即可浏览海量内容", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusXl))
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable { onNavigateToSourceManage() }
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(text = "导入视频源", style = MiuixTheme.textStyles.body1, color = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

/**
 * 首页内容 — 使用 LazyColumn 避免嵌套滚动冲突
 *
 * 视频卡片手动排成两列（Row 内两个 VideoCard），
 * 不使用 LazyVerticalGrid，彻底避免嵌套滚动崩溃。
 */
@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onVideoClick: (SearchResult) -> Unit,
    onNavigateToCategory: (String, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.cardGap)
    ) {
        // 源分类标签
        if (state.sources.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)) {
                    items(state.sources.size) { index ->
                        val source = state.sources[index]
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(DesignTokens.radiusXl))
                                .background(MiuixTheme.colorScheme.surfaceVariant)
                                .clickable { onNavigateToCategory(source.sourceName, "") }
                                .padding(horizontal = DesignTokens.spacingLg, vertical = DesignTokens.spacingSm)
                        ) {
                            Text(text = source.sourceName, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        if (state.videos.isNotEmpty()) {
            item {
                Text(text = "推荐", style = MiuixTheme.textStyles.headline1, color = MiuixTheme.colorScheme.onSurface)
            }
        }

        // 两列视频卡片
        val rows = state.videos.chunked(2)
        items(rows.size) { rowIndex ->
            val row = rows[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.cardGap)
            ) {
                row.forEach { video ->
                    VideoCard(
                        searchResult = video,
                        onClick = { onVideoClick(video) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 奇数个时填充空白
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
