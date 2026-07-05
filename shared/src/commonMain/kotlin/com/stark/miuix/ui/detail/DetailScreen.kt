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

package com.stark.miuix.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.icons.IconDownload
import com.stark.miuix.ui.icons.IconLike
import com.stark.miuix.ui.icons.IconRank
import com.stark.miuix.ui.icons.IconShare
import com.stark.miuix.ui.icons.IconStar
import com.stark.miuix.ui.icons.IconChat
import com.stark.miuix.ui.player.InlineVideoPlayer
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.UrlEncoder
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import top.yukonga.miuix.kmp.basic.Card
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.stark.miuix.ui.icons.IconBack
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频详情页
 *
 * 沉浸式封面 + 渐变蒙版 + 收藏按钮 + 剧集列表。
 * 进入详情页时自动记录到观看历史。
 */
@Composable
fun DetailScreen(
    sourceName: String,
    detailUrl: String,
    initialTitle: String,
    initialCoverUrl: String,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    userDataRepository: UserDataRepository,
    onNavigateToPlayer: (String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = remember(videoRepository, sourceRepository) {
        DetailViewModel(videoRepository, sourceRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val decodedUrl = UrlEncoder.decode(detailUrl)
    val decodedCover = UrlEncoder.decode(initialCoverUrl)
    val videoId = decodedUrl.hashCode().toString()
    var isFavorite by remember { mutableStateOf(userDataRepository.isFavorite(videoId)) }
    val shareAction = com.stark.miuix.util.rememberShareAction()
    var descExpanded by remember { mutableStateOf(false) }
    var episodesExpanded by remember { mutableStateOf(false) }
    var selectedEpisodeIndex by remember { mutableStateOf(-1) }
    var playingEpisodeUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(detailUrl) {
        viewModel.loadDetail(sourceName, decodedUrl)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = initialTitle.ifBlank { "视频详情" },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    androidx.compose.foundation.Image(painter=rememberVectorPainter(IconBack),contentDescription="返回",colorFilter=ColorFilter.tint(MiuixTheme.colorScheme.onSurface),modifier=androidx.compose.ui.Modifier.size(20.dp))
                }
            },
            actions = {
                IconButton(onClick = {
                    shareAction(initialTitle, "$initialTitle\n$detailUrl")
                }) {
                    Text("分享", style = MiuixTheme.textStyles.body2)
                }
            }
        )

        when (val state = uiState) {
            is DetailUiState.Loading -> {
                ShimmerVideoGrid(columns = 1, itemCount = 1)
            }

            is DetailUiState.Success -> {
                val video = state.video

                // 加载成功后记录观看历史
                LaunchedEffect(video) {
                    userDataRepository.addWatchHistory(
                        WatchHistory(
                            videoId = videoId,
                            title = video.title,
                            cover = video.cover,
                            sourceName = sourceName,
                            detailUrl = decodedUrl
                        )
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // 顶部：内嵌播放器 OR 封面图
                    item {
                        val currentUrl = playingEpisodeUrl
                        if (currentUrl != null) {
                            // 内嵌播放器 — 点击全屏按钮才跳转 PlayerScreen
                            InlineVideoPlayer(
                                url = currentUrl,
                                title = video.title,
                                modifier = Modifier.fillMaxWidth(),
                                onRequestFullscreen = {
                                    onNavigateToPlayer(state.currentSource, currentUrl, video.title)
                                }
                            )
                        } else {
                            // 封面图 + 渐变蒙版
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(DesignTokens.videoBackground)
                            ) {
                                if (video.cover.isNotBlank() && (video.cover.startsWith("http://") || video.cover.startsWith("https://"))) {
                                    val ctx = LocalPlatformContext.current
                                    AsyncImage(
                                        model = ImageRequest.Builder(ctx).data(video.cover).build(),
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                            startY = 80f
                                        ))
                                )
                                Text(
                                    text = video.title,
                                    style = MiuixTheme.textStyles.headline1,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                                )
                            }
                        }
                    }

                    // 视频信息卡片
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            cornerRadius = 12.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (video.status.isNotBlank()) {
                                    Text(
                                        text = video.status,
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (video.description.isNotBlank()) {
                                    Text(
                                        text = video.description,
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = if (descExpanded) Int.MAX_VALUE else 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (descExpanded) "收起" else "展开",
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable { descExpanded = !descExpanded }
                                    )
                                }
                            }
                        }
                    }

                    // 播放源选择器（多源时显示）
                    if (state.allSources.size > 1) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = "播放源 (${state.allSources.size})",
                                    style = MiuixTheme.textStyles.body1,
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.allSources.keys.forEach { name ->
                                        val isSelected = name == state.currentSource
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(DesignTokens.radiusXl))
                                                .background(
                                                    if (isSelected) MiuixTheme.colorScheme.primary
                                                    else MiuixTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { viewModel.switchSource(name) }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                style = MiuixTheme.textStyles.body2,
                                                color = if (isSelected) Color.White
                                                        else MiuixTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 选集区 — FlowRow 网格，支持展开/收起（移除横滑）
                    if (video.episodes.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm)
                            ) {
                                // 标题行
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "选集",
                                        style = MiuixTheme.textStyles.body1,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val displayCount = if (episodesExpanded) video.episodes.size else minOf(video.episodes.size, 20)
                                    Text(
                                        text = if (episodesExpanded) "收起 ∧" else "全 ${video.episodes.size} 集 ∨",
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = DesignTokens.brandBlue,
                                        modifier = Modifier.clickable { episodesExpanded = !episodesExpanded }
                                            .padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                                // FlowRow 集数网格
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
                                    verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
                                ) {
                                    val displayEpisodes = if (episodesExpanded) video.episodes
                                                          else video.episodes.take(20)
                                    displayEpisodes.forEachIndexed { index, episode ->
                                        val isSelected = index == selectedEpisodeIndex
                                        Box(
                                            modifier = Modifier
                                                .widthIn(min = 52.dp)
                                                .clip(RoundedCornerShape(DesignTokens.radiusSm))
                                                .background(
                                                    if (isSelected) DesignTokens.brandBlue
                                                    else MiuixTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable {
                                                    selectedEpisodeIndex = index
                                                    playingEpisodeUrl = episode.url  // 触发内嵌播放
                                                }
                                                .padding(horizontal = DesignTokens.spacingMd, vertical = DesignTokens.spacingSm),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = episode.name.ifBlank { "${index + 1}" },
                                                style = MiuixTheme.textStyles.body2,
                                                color = if (isSelected) Color.White
                                                        else MiuixTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 底部操作栏（对标设计图：收藏/换源/分享）
                    item {
                        Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 收藏按钮 — 修复：调用正确的 toggleFavorite
                            ActionButton(
                                icon = if (isFavorite) IconStar else IconLike,
                                label = if (isFavorite) "已收藏" else "收藏",
                                tint = if (isFavorite) DesignTokens.brandBlue else MiuixTheme.colorScheme.onSurface
                            ) {
                                val fav = Favorite(
                                    videoId = videoId, title = video.title,
                                    cover = video.cover, sourceName = sourceName, detailUrl = decodedUrl
                                )
                                isFavorite = userDataRepository.toggleFavorite(fav)
                            }
                            ActionButton(IconChat, "评论") {}
                            ActionButton(IconShare, "分享") { shareAction(video.title, "${video.title}\n$decodedUrl") }
                            ActionButton(IconDownload, "缓存") {}
                            ActionButton(IconRank, "排行") {}
                        }
                        Spacer(modifier = Modifier.height(DesignTokens.spacingXl))
                    }
                }
            }

            is DetailUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.loadDetail(sourceName, decodedUrl) }
                )
            }
        }
    }
}

/** 底部操作按钮 — 44dp 最小触控区域 */
@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .widthIn(min = 44.dp)
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon),
            contentDescription = label,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote2,
            color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.outline)
    }
}
