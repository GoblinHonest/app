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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import com.stark.miuix.ui.icons.IconSearch
import com.stark.miuix.ui.theme.DesignTokens
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

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

    Column(
        modifier = Modifier.fillMaxSize()  // 不在此加 statusBar padding，由 SearchBar 内部处理
    ) {
        // 顶部搜索栏
        SearchBar(onSearchClick = onNavigateToSearch)

        when (val state = uiState) {
            is HomeUiState.Loading -> ShimmerVideoGrid()
            is HomeUiState.Success -> {
                if (state.sources.isEmpty()) {
                    EmptySourceHint(onNavigateToSourceManage)
                } else {
                    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                    val pullState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
                    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize(),
                        state = pullState,
                        indicator = {
                            // 蓝色指示器，与顶部 brandBlue 渐变一致
                            androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator(
                                state = pullState,
                                isRefreshing = isRefreshing,
                                color = DesignTokens.brandBlue,
                                containerColor = MiuixTheme.colorScheme.surface,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    ) {
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
            }
            is HomeUiState.Error -> ErrorContent(state.message) { viewModel.loadVideos() }
        }
    }
}

@Composable
private fun SearchBar(
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DesignTokens.brandBlue.copy(alpha = 0.12f),
                        MiuixTheme.colorScheme.surface.copy(alpha = 0f)
                    )
                )
            )
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 搜索框 — pill 44dp，全宽，下拉刷新替代独立刷新按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MiuixTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSearchClick)
                .padding(horizontal = DesignTokens.spacingLg),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = rememberVectorPainter(IconSearch),
                    contentDescription = "搜索",
                    modifier = Modifier.size(18.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MiuixTheme.colorScheme.outline)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "搜索视频、演员...",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.outline
                )
            }
        }
    }
    } // Box gradient
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

        // 推荐标题 — 左侧 4dp 蓝色竖线 + 「精选推荐」字号加大
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = DesignTokens.spacingXs)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DesignTokens.brandBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "精选推荐",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
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
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.padding(vertical = DesignTokens.spacingXs)
    ) {
        itemsIndexed(sources) { index, source ->
            val isSelected = index == selectedIndex
            // 胶囊填充样式：选中时 brandBlue 背景白字，未选中时透明背景主题色文字
            Box(
                modifier = Modifier
                    .padding(horizontal = DesignTokens.spacingXs, vertical = DesignTokens.spacingXs)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) DesignTokens.brandBlue
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = DesignTokens.spacingMd, vertical = 6.dp),
                contentAlignment = Alignment.Center
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
            val ctx = LocalPlatformContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(coverUrl).build(),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
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

        // 左下角「热播」红色 badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = if (video.description.isNotBlank()) 52.dp else 36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DesignTokens.badgeRed)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "热播",
                style = MiuixTheme.textStyles.footnote2,
                color = Color.White
            )
        }

        // 右侧播放按钮——使用 IconPlay SVG（更清晰）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberVectorPainter(com.stark.miuix.ui.icons.IconPlay),
                contentDescription = "播放",
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptySourceHint(onNavigateToSourceManage: () -> Unit) {
    com.stark.miuix.ui.components.EmptyStateView(
        title = "暂无视频源",
        message = "导入视频源后即可开始使用",
        actionText = "管理源",
        onAction = onNavigateToSourceManage
    )
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    com.stark.miuix.ui.components.EmptyStateView(
        title = "加载失败",
        message = message,
        actionText = "重试",
        onAction = onRetry
    )
}
