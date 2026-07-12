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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
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
 * 首页 — 分类 Tab + 推荐网格
 *
 * 结构（从上到下）：
 * 1. 搜索栏（点击跳搜索页）
 * 2. 分类横滑 Tab（推荐 | 电视剧 | 动漫 | 电影）
 * 3. Banner 区域（仅推荐 Tab 显示）
 * 4. 视频网格（3 列竖向海报卡片）
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
    val currentCategory by viewModel.currentCategory.collectAsState()
    val sources by sourceRepository.sources.collectAsState()

    val sourceCount = sources.size
    // 仅在 sources 稳定后才触���加载（避免多次 addSource 导致的反复取消）
    val hasSources = sources.isNotEmpty()
    LaunchedEffect(hasSources) {
        if (hasSources) {
            com.stark.miuix.util.AppLogger.d("HomeUI", "LaunchedEffect 触发 loadVideos, sources=${sources.size}")
            viewModel.loadVideos()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部搜索栏
        SearchBar(onSearchClick = onNavigateToSearch)

        // 分类 Tab
        CategoryTabs(
            selectedCategory = currentCategory,
            onSelect = { viewModel.switchCategory(it) }
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                com.stark.miuix.util.AppLogger.d("HomeUI", "显示 Loading (shimmer)")
                ShimmerVideoGrid()
            }
            is HomeUiState.Success -> {
                com.stark.miuix.util.AppLogger.d("HomeUI", "显示 Success: ${state.videos.size} 条, category=${state.currentCategory}")
                if (sources.isEmpty()) {
                    EmptySourceHint(onNavigateToSourceManage)
                } else {
                    HomeContentFeed(
                        videos = state.videos,
                        showBanner = currentCategory == "推荐",
                        sectionTitle = if (currentCategory == "推荐") "精选推荐" else currentCategory,
                        onVideoClick = { video ->
                            onNavigateToDetail(
                                video.sourceName, video.url, video.title, video.cover
                            )
                        }
                    )
                }
            }
            is HomeUiState.Error -> {
                com.stark.miuix.util.AppLogger.e("HomeUI", "显示 Error: ${state.message}")
                ErrorContent(state.message) { viewModel.loadVideos() }
            }
        }

    }
}

@Composable
private fun SearchBar(onSearchClick: () -> Unit) {
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
                    Image(
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
    }
}

/** 分类 Tab — 固定 4 个：推荐 | 电视剧 | 动漫 | 电影 */
@Composable
private fun CategoryTabs(
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.padding(vertical = DesignTokens.spacingXs)
    ) {
        itemsIndexed(HOME_TABS) { _, category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .padding(horizontal = DesignTokens.spacingXs, vertical = DesignTokens.spacingXs)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) DesignTokens.brandBlue else Color.Transparent
                    )
                    .clickable { onSelect(category) }
                    .padding(horizontal = DesignTokens.spacingMd, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    style = MiuixTheme.textStyles.body2,
                    color = if (isSelected) Color.White
                           else MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}

@Composable
private fun HomeContentFeed(
    videos: List<SearchResult>,
    showBanner: Boolean,
    sectionTitle: String,
    onVideoClick: (SearchResult) -> Unit
) {
    val bannerVideo = if (showBanner) videos.firstOrNull() else null

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(
            horizontal = DesignTokens.screenPadding,
            vertical = DesignTokens.spacingSm
        ),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingMd),
        modifier = Modifier.fillMaxSize()
    ) {
        // Banner 大图（仅推荐 Tab）
        if (bannerVideo != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BannerCard(
                    video = bannerVideo,
                    onClick = { onVideoClick(bannerVideo) }
                )
            }
        }

        // 分类标题
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
                    text = "$sectionTitle (${videos.size})",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }

        // 3 列视频卡片
        val gridVideos = if (bannerVideo != null) videos.drop(1) else videos
        items(
            items = gridVideos,
            key = { "${it.sourceName}:${it.url}" }
        ) { video ->
            VideoCard(
                searchResult = video,
                onClick = { onVideoClick(video) }
            )
        }
    }
}

/** Banner 大图卡片 — 16:9 横向封面 + 底部渐变标题 */
@Composable
private fun BannerCard(video: SearchResult, onClick: () -> Unit) {
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
