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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoCard
import com.stark.miuix.ui.theme.DesignTokens
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 首页 — 参考优酷 App 设计
 *
 * 结构（从上到下）：
 * 1. 搜索栏（点击跳搜索页）
 * 2. 分类横滑 Tab（按视频源名称）
 * 3. Banner 区域（首个视频的封面大图）
 * 4. 推荐网格（3 列竖向海报卡片）
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

    val sourceCount = sources.size
    LaunchedEffect(sourceCount) {
        viewModel.loadVideos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // 顶部搜索栏
        SearchBar(
            isRefreshing = isRefreshing,
            onSearchClick = onNavigateToSearch,
            onRefreshClick = { viewModel.refresh() },
            onSourceClick = onNavigateToSourceManage
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> ShimmerVideoGrid()
            is HomeUiState.Success -> {
                if (state.sources.isEmpty()) {
                    EmptySourceHint(onNavigateToSourceManage)
                } else {
                    HomeContentFeed(
                        state = state,
                        onVideoClick = { video ->
                            onNavigateToDetail(
                                video.sourceName, video.url, video.title, video.cover
                            )
                        },
                        onCategoryClick = onNavigateToCategory
                    )
                }
            }
            is HomeUiState.Error -> ErrorContent(state.message) { viewModel.loadVideos() }
        }
    }
}

@Composable
private fun SearchBar(
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
        // 搜索框
        Box(
            modifier = Modifier
                .weight(1f)
                .height(DesignTokens.searchBarHeight)
                .clip(RoundedCornerShape(DesignTokens.radiusXl))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSearchClick)
                .padding(horizontal = DesignTokens.spacingLg),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "搜索视频、演员...",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.outline
            )
        }
        // 管理源
        Box(
            modifier = Modifier
                .height(DesignTokens.searchBarHeight)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSourceClick)
                .padding(horizontal = DesignTokens.spacingMd),
            contentAlignment = Alignment.Center
        ) {
            Text("源", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun HomeContentFeed(
    state: HomeUiState.Success,
    onVideoClick: (SearchResult) -> Unit,
    onCategoryClick: (String, String) -> Unit
) {
    var selectedSource by remember { mutableIntStateOf(0) }
    val currentSource = state.sources.getOrNull(selectedSource)
    val filteredVideos = if (currentSource != null) {
        state.videos.filter { it.sourceName == currentSource.sourceName }
            .ifEmpty { state.videos }
    } else {
        state.videos
    }
    val bannerVideo = filteredVideos.firstOrNull()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(
            horizontal = DesignTokens.screenPadding,
            vertical = DesignTokens.spacingSm
        ),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingMd)
    ) {
        // 分类 Tab 横滑
        if (state.sources.size > 1) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryTabs(
                    sources = state.sources,
                    selectedIndex = selectedSource,
                    onSelect = { selectedSource = it }
                )
            }
        }

        // Banner 大图（占满整行）
        if (bannerVideo != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BannerCard(
                    video = bannerVideo,
                    onClick = { onVideoClick(bannerVideo) }
                )
            }
        }

        // 推荐标题
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "推荐",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = DesignTokens.spacingXs)
            )
        }

        // 3 列视频卡片（跳过第一个已展示在 Banner 里的）
        items(
            items = filteredVideos.drop(1),
            key = { "${it.sourceName}:${it.url}" }
        ) { video ->
            VideoCard(
                searchResult = video,
                onClick = { onVideoClick(video) }
            )
        }
    }
}

@Composable
private fun CategoryTabs(
    sources: List<VideoSource>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
        modifier = Modifier.padding(vertical = DesignTokens.spacingXs)
    ) {
        itemsIndexed(sources) { index, source ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusXl))
                    .background(
                        if (isSelected) MiuixTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = DesignTokens.spacingMd, vertical = DesignTokens.spacingXs)
            ) {
                Text(
                    text = source.sourceName,
                    style = MiuixTheme.textStyles.body2,
                    color = if (isSelected) Color.White
                           else MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}

/** Banner 大图卡片 — 16:9 横向封面 + 底部渐变标题 */
@Composable
private fun BannerCard(
    video: SearchResult,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(DesignTokens.radiusLg))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        val coverUrl = video.cover
        if (coverUrl.isNotBlank() &&
            (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))
        ) {
            val painterResource = asyncPainterResource(data = coverUrl)
            KamelImage(
                resource = painterResource,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onFailure = { }
            )
        }

        // 底部渐变 + 标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = video.title,
                style = MiuixTheme.textStyles.body1,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (video.description.isNotBlank()) {
                Text(
                    text = video.description,
                    style = MiuixTheme.textStyles.footnote1,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptySourceHint(onNavigateToSourceManage: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("暂无视频源", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            Text("请先导入视频源", style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(DesignTokens.spacingXl))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusXl))
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable { onNavigateToSourceManage() }
                    .padding(horizontal = 32.dp, vertical = DesignTokens.spacingMd)
            ) {
                Text("导入视频源", style = MiuixTheme.textStyles.body1, color = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("加载失败", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            Text(message, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusXl))
                    .background(MiuixTheme.colorScheme.primary)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 24.dp, vertical = DesignTokens.spacingSm)
            ) {
                Text("重试", style = MiuixTheme.textStyles.body2, color = Color.White)
            }
        }
    }
}
