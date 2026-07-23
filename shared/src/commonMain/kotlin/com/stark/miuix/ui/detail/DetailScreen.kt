/*
 * Copyright 2024 Stark Industries
 *
 * 视频详情页 — 参考播放详情布局：
 * 顶部宽海报 → 竖海报+信息 → 标签操作 → 功能五宫格 → 线路 → 选集宫格
 * 风格：Miuix + DesignTokens / AppColors；保留内嵌播放与全屏。
 */
package com.stark.miuix.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.stark.miuix.data.model.Episode
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.Video
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.model.WatchProgress
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.di.AppContainer
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconPlay
import com.stark.miuix.ui.icons.IconShare
import com.stark.miuix.ui.icons.IconStar
import com.stark.miuix.ui.player.FullscreenControls
import com.stark.miuix.ui.player.InlineVideoPlayer
import com.stark.miuix.ui.player.releaseSharedPlayer
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.AppBackHandler
import com.stark.miuix.util.UrlEncoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DetailScreen(
    sourceName: String,
    detailUrl: String,
    initialTitle: String,
    initialCoverUrl: String,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    userDataRepository: UserDataRepository,
    onNavigateToPlayer: (String, String, String, Long) -> Unit,
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
    val coroutineScope = rememberCoroutineScope()
    var descExpanded by remember { mutableStateOf(false) }
    var episodesExpanded by remember { mutableStateOf(false) }
    var selectedEpisodeIndex by remember { mutableStateOf(0) }
    var selectedLineIndex by remember { mutableStateOf(0) }
    var resolvedVideoUrl by remember { mutableStateOf<String?>(null) }
    var playerLoading by remember { mutableStateOf(true) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var inlinePlayerPosition by remember { mutableLongStateOf(0L) }
    var inlinePlayerDuration by remember { mutableLongStateOf(0L) }
    var resumePositionMs by remember { mutableLongStateOf(0L) }
    var isPlayerFullscreen by remember { mutableStateOf(false) }
    var isVideoBuffering by remember { mutableStateOf(false) }
    var episodesReversed by remember { mutableStateOf(false) }
    var autoPlayStarted by remember(detailUrl) { mutableStateOf(false) }

    LaunchedEffect(detailUrl) {
        viewModel.loadDetail(sourceName, decodedUrl)
        autoPlayStarted = false
        resolvedVideoUrl = null
        playerLoading = true
        playerError = null
        selectedEpisodeIndex = 0
        selectedLineIndex = 0
        resumePositionMs = 0L
    }

    // 返回优先级：关弹层 → 退出全屏 → 离开详情页
    AppBackHandler(enabled = episodesExpanded) {
        episodesExpanded = false
    }
    AppBackHandler(enabled = descExpanded) {
        descExpanded = false
    }
    AppBackHandler(enabled = isPlayerFullscreen) {
        isPlayerFullscreen = false
    }

    fun playEpisode(
        src: String,
        index: Int,
        episode: Episode,
        startMs: Long = 0L,
        updateHistory: Boolean = true
    ) {
        selectedEpisodeIndex = index
        resumePositionMs = startMs.coerceAtLeast(0L)
        playerLoading = true
        playerError = null
        coroutineScope.launch {
            val result = viewModel.getPlayerUrl(src, episode.url)
            result.fold(
                onSuccess = { streamUrl ->
                    resolvedVideoUrl = streamUrl
                    playerLoading = false
                    if (updateHistory) {
                        val video = (uiState as? DetailUiState.Success)?.video
                        userDataRepository.addWatchHistory(
                            WatchHistory(
                                videoId = videoId,
                                title = video?.title?.ifBlank { initialTitle } ?: initialTitle,
                                cover = video?.cover?.ifBlank { decodedCover } ?: decodedCover,
                                sourceName = sourceName,
                                detailUrl = decodedUrl,
                                lastEpisode = episode.name.ifBlank { "第${index + 1}集" }
                            )
                        )
                        userDataRepository.saveProgress(
                            WatchProgress(
                                videoId = videoId,
                                episodeIndex = index,
                                episodeName = episode.name.ifBlank { "第${index + 1}集" },
                                positionMs = startMs.coerceAtLeast(0L),
                                durationMs = inlinePlayerDuration,
                                sourceName = sourceName,
                                detailUrl = decodedUrl,
                                title = video?.title?.ifBlank { initialTitle } ?: initialTitle,
                                cover = video?.cover?.ifBlank { decodedCover } ?: decodedCover
                            )
                        )
                    }
                },
                onFailure = { e ->
                    playerLoading = false
                    playerError = e.message ?: "播放地址解析失败"
                }
            )
        }
    }

    // 播放中定期保存进度（续播）
    LaunchedEffect(resolvedVideoUrl, selectedEpisodeIndex, selectedLineIndex) {
        if (resolvedVideoUrl == null) return@LaunchedEffect
        while (true) {
            delay(5_000)
            val video = (uiState as? DetailUiState.Success)?.video ?: continue
            val episodes = if (video.playLines.isNotEmpty() &&
                selectedLineIndex in video.playLines.indices
            ) {
                video.playLines[selectedLineIndex].episodes
            } else {
                video.episodes
            }
            val ep = episodes.getOrNull(selectedEpisodeIndex)
            if (ep != null && inlinePlayerPosition > 0L) {
                userDataRepository.saveProgress(
                    WatchProgress(
                        videoId = videoId,
                        episodeIndex = selectedEpisodeIndex,
                        episodeName = ep.name.ifBlank { "第${selectedEpisodeIndex + 1}集" },
                        positionMs = inlinePlayerPosition,
                        durationMs = inlinePlayerDuration,
                        sourceName = sourceName,
                        detailUrl = decodedUrl,
                        title = video.title.ifBlank { initialTitle },
                        cover = video.cover.ifBlank { decodedCover }
                    )
                )
                userDataRepository.addWatchHistory(
                    WatchHistory(
                        videoId = videoId,
                        title = video.title.ifBlank { initialTitle },
                        cover = video.cover.ifBlank { decodedCover },
                        sourceName = sourceName,
                        detailUrl = decodedUrl,
                        lastEpisode = ep.name.ifBlank { "第${selectedEpisodeIndex + 1}集" },
                        progress = if (inlinePlayerDuration > 0) {
                            (inlinePlayerPosition.toFloat() / inlinePlayerDuration).coerceIn(0f, 1f)
                        } else 0f
                    )
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // 离开详情时落盘最后进度并释放共享播放器
            val video = (uiState as? DetailUiState.Success)?.video
            val episodes = video?.let {
                if (it.playLines.isNotEmpty() && selectedLineIndex in it.playLines.indices) {
                    it.playLines[selectedLineIndex].episodes
                } else it.episodes
            }.orEmpty()
            val ep = episodes.getOrNull(selectedEpisodeIndex)
            if (video != null && ep != null && inlinePlayerPosition > 0L) {
                userDataRepository.saveProgress(
                    WatchProgress(
                        videoId = videoId,
                        episodeIndex = selectedEpisodeIndex,
                        episodeName = ep.name.ifBlank { "第${selectedEpisodeIndex + 1}集" },
                        positionMs = inlinePlayerPosition,
                        durationMs = inlinePlayerDuration,
                        sourceName = sourceName,
                        detailUrl = decodedUrl,
                        title = video.title.ifBlank { initialTitle },
                        cover = video.cover.ifBlank { decodedCover }
                    )
                )
            }
            releaseSharedPlayer()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
    ) {
        if (!isPlayerFullscreen) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DetailTopBar(onNavigateBack = onNavigateBack)
                        ShimmerVideoGrid(columns = 1, itemCount = 2)
                    }
                }

                is DetailUiState.Error -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DetailTopBar(onNavigateBack = onNavigateBack)
                        ErrorStateView(
                            message = state.message,
                            onRetry = { viewModel.loadDetail(sourceName, decodedUrl) }
                        )
                    }
                }

                is DetailUiState.Success -> {
                    val video = state.video
                    val lineEpisodes = remember(video, selectedLineIndex) {
                        if (video.playLines.isNotEmpty() &&
                            selectedLineIndex in video.playLines.indices
                        ) {
                            video.playLines[selectedLineIndex].episodes
                        } else {
                            video.episodes
                        }
                    }
                    // 宫格展示用（可倒序），实际播放索引始终对应 lineEpisodes 正序
                    val displayEpisodes = remember(lineEpisodes, episodesReversed) {
                        if (episodesReversed) lineEpisodes.asReversed() else lineEpisodes
                    }

                    // 进入详情：默认第一集；有续播记录则恢复集数与进度
                    LaunchedEffect(video, lineEpisodes) {
                        if (autoPlayStarted || lineEpisodes.isEmpty()) return@LaunchedEffect
                        autoPlayStarted = true
                        val progress = userDataRepository.getProgress(videoId)
                        val targetIndex = progress
                            ?.episodeIndex
                            ?.takeIf { it in lineEpisodes.indices }
                            ?: 0
                        val startMs = progress
                            ?.takeIf { !it.isCompleted && it.episodeIndex == targetIndex }
                            ?.positionMs
                            ?.coerceAtLeast(0L)
                            ?: 0L
                        playEpisode(
                            src = state.currentSource,
                            index = targetIndex,
                            episode = lineEpisodes[targetIndex],
                            startMs = startMs,
                            updateHistory = true
                        )
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // 顶部常驻播放器（无海报）
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val currentUrl = resolvedVideoUrl
                            if (currentUrl != null) {
                                InlineVideoPlayer(
                                    url = currentUrl,
                                    title = video.title.ifBlank { initialTitle },
                                    modifier = Modifier.fillMaxWidth(),
                                    onRequestFullscreen = { isPlayerFullscreen = true },
                                    isLoading = playerLoading,
                                    errorMessage = playerError,
                                    onPositionChanged = { pos -> inlinePlayerPosition = pos },
                                    isFullscreen = false,
                                    startPositionMs = resumePositionMs,
                                    onDurationChanged = { d -> inlinePlayerDuration = d }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (playerLoading) {
                                        Text(
                                            text = "正在解析播放地址…",
                                            style = MiuixTheme.textStyles.body2,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    } else if (!playerError.isNullOrBlank()) {
                                        Text(
                                            text = playerError ?: "播放失败",
                                            style = MiuixTheme.textStyles.body2,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    } else {
                                        Text(
                                            text = "准备播放…",
                                            style = MiuixTheme.textStyles.body2,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .align(Alignment.TopStart)
                            ) {
                                Image(
                                    painter = rememberVectorPainter(IconBack),
                                    contentDescription = "返回",
                                    colorFilter = ColorFilter.tint(Color.White),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // 信息区：竖海报 + 元数据
                            item {
                                InfoSection(
                                    video = video,
                                    fallbackTitle = initialTitle,
                                    fallbackCover = decodedCover
                                )
                            }

                            // 状态标签 + 收藏 / 简介
                            item {
                                TagAndActionsRow(
                                    video = video,
                                    isFavorite = isFavorite,
                                    onToggleFavorite = {
                                        val fav = Favorite(
                                            videoId = videoId,
                                            title = video.title.ifBlank { initialTitle },
                                            cover = video.cover.ifBlank { decodedCover },
                                            sourceName = sourceName,
                                            detailUrl = decodedUrl
                                        )
                                        isFavorite = userDataRepository.toggleFavorite(fav)
                                    },
                                    onShowDesc = { descExpanded = true }
                                )
                            }

                            // 仅保留有实际功能的操作
                            item {
                                ActionGrid(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = {
                                        val fav = Favorite(
                                            videoId = videoId,
                                            title = video.title.ifBlank { initialTitle },
                                            cover = video.cover.ifBlank { decodedCover },
                                            sourceName = sourceName,
                                            detailUrl = decodedUrl
                                        )
                                        isFavorite = userDataRepository.toggleFavorite(fav)
                                    },
                                    onShare = {
                                        shareAction(
                                            video.title.ifBlank { initialTitle },
                                            "${video.title.ifBlank { initialTitle }}\n$decodedUrl"
                                        )
                                    },
                                    onFullscreen = {
                                        if (resolvedVideoUrl != null) {
                                            isPlayerFullscreen = true
                                        } else if (lineEpisodes.isNotEmpty()) {
                                            val idx = selectedEpisodeIndex
                                                .takeIf { it in lineEpisodes.indices }
                                                ?: 0
                                            playEpisode(
                                                state.currentSource,
                                                idx,
                                                lineEpisodes[idx]
                                            )
                                            isPlayerFullscreen = true
                                        }
                                    }
                                )
                            }

                            // 播放源（多源时）
                            if (state.allSources.size > 1) {
                                item {
                                    SourceChips(
                                        sources = state.allSources.keys.toList(),
                                        current = state.currentSource,
                                        onSelect = { name ->
                                            viewModel.switchSource(name)
                                            selectedLineIndex = 0
                                            selectedEpisodeIndex = 0
                                            resolvedVideoUrl = null
                                            autoPlayStarted = false
                                            playerLoading = true
                                        }
                                    )
                                }
                            }

                            // 线路
                            if (video.playLines.size > 1) {
                                item {
                                    LineChips(
                                        lines = video.playLines.map { it.name },
                                        selectedIndex = selectedLineIndex,
                                        onSelect = { index ->
                                            selectedLineIndex = index
                                            selectedEpisodeIndex = 0
                                            resolvedVideoUrl = null
                                            autoPlayStarted = false
                                            playerLoading = true
                                        }
                                    )
                                }
                            }

                            // 选集宫格
                            if (displayEpisodes.isNotEmpty()) {
                                item {
                                    EpisodeSection(
                                        episodes = displayEpisodes,
                                        // 倒序展示时，选中高亮映射到展示索引
                                        selectedIndex = if (episodesReversed) {
                                            (lineEpisodes.size - 1 - selectedEpisodeIndex)
                                                .coerceIn(0, (lineEpisodes.size - 1).coerceAtLeast(0))
                                        } else {
                                            selectedEpisodeIndex
                                        },
                                        reversed = episodesReversed,
                                        onToggleReverse = { episodesReversed = !episodesReversed },
                                        onExpand = { episodesExpanded = true },
                                        onSelect = { displayIndex, episode ->
                                            val realIndex = if (episodesReversed) {
                                                lineEpisodes.indexOfFirst { it.url == episode.url }
                                                    .takeIf { it >= 0 } ?: displayIndex
                                            } else {
                                                displayIndex
                                            }
                                            playEpisode(state.currentSource, realIndex, episode)
                                        }
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(88.dp)) }
                        }
                    }
                }
            }
        } else {
            val currentUrl = resolvedVideoUrl
            if (currentUrl != null) {
                val video = (uiState as? DetailUiState.Success)?.video
                val fullscreenTitle = buildString {
                    append(video?.title ?: initialTitle)
                    val eps = video?.let {
                        if (it.playLines.isNotEmpty() && selectedLineIndex in it.playLines.indices) {
                            it.playLines[selectedLineIndex].episodes
                        } else it.episodes
                    }.orEmpty()
                    val epName = eps.getOrNull(selectedEpisodeIndex)?.name
                    if (!epName.isNullOrBlank()) append(" - $epName")
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    InlineVideoPlayer(
                        url = currentUrl,
                        title = fullscreenTitle,
                        modifier = Modifier.fillMaxSize(),
                        onRequestFullscreen = {},
                        isLoading = false,
                        errorMessage = null,
                        onPositionChanged = { pos -> inlinePlayerPosition = pos },
                        isFullscreen = true,
                        onBufferingChanged = { isVideoBuffering = it },
                        startPositionMs = 0L,
                        onDurationChanged = { d -> inlinePlayerDuration = d }
                    )
                    FullscreenControls(
                        url = currentUrl,
                        title = fullscreenTitle,
                        onExitFullscreen = { isPlayerFullscreen = false },
                        isBuffering = isVideoBuffering,
                        dlnaController = AppContainer.dlnaController
                    )
                }
            }
        }

        // 简介弹层
        val successVideo = (uiState as? DetailUiState.Success)?.video
        if (!isPlayerFullscreen && descExpanded && successVideo != null) {
            DescSheet(
                video = successVideo,
                onDismiss = { descExpanded = false }
            )
        }

        // 全集 Bottom Sheet
        if (!isPlayerFullscreen) {
            val sheetVideo = (uiState as? DetailUiState.Success)?.video
            val sheetBase = if (sheetVideo != null &&
                sheetVideo.playLines.isNotEmpty() &&
                selectedLineIndex in sheetVideo.playLines.indices
            ) {
                sheetVideo.playLines[selectedLineIndex].episodes
            } else {
                sheetVideo?.episodes.orEmpty()
            }
            val sheetEpisodes = if (episodesReversed) sheetBase.asReversed() else sheetBase
            val sheetSelected = if (episodesReversed) {
                (sheetBase.size - 1 - selectedEpisodeIndex)
                    .coerceIn(0, (sheetBase.size - 1).coerceAtLeast(0))
            } else {
                selectedEpisodeIndex
            }
            EpisodeSheetOverlay(
                visible = episodesExpanded && sheetVideo != null,
                episodes = sheetEpisodes,
                selectedEpisodeIndex = sheetSelected,
                onDismiss = { episodesExpanded = false },
                onSelectEpisode = { displayIndex, episode ->
                    episodesExpanded = false
                    val src = (uiState as? DetailUiState.Success)?.currentSource ?: ""
                    val realIndex = if (episodesReversed) {
                        sheetBase.indexOfFirst { it.url == episode.url }
                            .takeIf { it >= 0 } ?: displayIndex
                    } else {
                        displayIndex
                    }
                    playEpisode(src, realIndex, episode)
                }
            )
        }
    }
}

// ─── Top bar (loading / error) ───

@Composable
private fun DetailTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Image(
                painter = rememberVectorPainter(IconBack),
                contentDescription = "返回",
                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Info: poster + meta ───

@Composable
private fun InfoSection(
    video: Video,
    fallbackTitle: String,
    fallbackCover: String
) {
    val cover = video.cover.ifBlank { fallbackCover }
    val title = video.title.ifBlank { fallbackTitle }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingMd, bottom = DesignTokens.spacingSm),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .aspectRatio(DesignTokens.coverAspectRatio)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant)
        ) {
            if (cover.isNotBlank() && (cover.startsWith("http://") || cover.startsWith("https://"))) {
                val ctx = LocalPlatformContext.current
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(cover).build(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.headline1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            val yearLine = buildList {
                if (video.year.isNotBlank()) add(video.year)
                if (video.area.isNotBlank()) add(video.area)
                if (video.category.isNotBlank()) add(video.category)
            }.joinToString(" · ")
            if (yearLine.isNotBlank()) {
                Text(
                    text = yearLine,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (video.genres.isNotEmpty()) {
                Text(
                    text = video.genres.take(6).joinToString(" / "),
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (video.description.isNotBlank()) {
                Text(
                    text = video.description,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── 3. Tags + favorite / intro ───

@Composable
private fun TagAndActionsRow(
    video: Video,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShowDesc: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (video.status.isNotBlank()) {
                StatusChip(text = video.status)
            }
            if (video.sourceName.isNotBlank()) {
                StatusChip(text = video.sourceName, subtle = true)
            }
        }
        Text(
            text = if (isFavorite) "已收藏" else "收藏",
            style = MiuixTheme.textStyles.footnote1,
            color = if (isFavorite) AppColors.brand() else MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier
                .clickable(onClick = onToggleFavorite)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
        Text(
            text = "|",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.outline
        )
        Text(
            text = "简介 >",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier
                .clickable(onClick = onShowDesc)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatusChip(text: String, subtle: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(DesignTokens.radiusXs))
            .background(
                if (subtle) MiuixTheme.colorScheme.surfaceVariant
                else AppColors.brand().copy(alpha = 0.15f)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote2,
            color = if (subtle) MiuixTheme.colorScheme.onSurfaceVariantSummary else AppColors.brand()
        )
    }
}

// ─── 4. Action grid ───

@Composable
private fun ActionGrid(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onFullscreen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingMd),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionItem(
            icon = IconStar,
            label = if (isFavorite) "已收藏" else "收藏",
            highlighted = isFavorite,
            onClick = onToggleFavorite
        )
        ActionItem(IconShare, "分享", onClick = onShare)
        ActionItem(IconPlay, "全屏", onClick = onFullscreen)
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (highlighted) AppColors.brand() else MiuixTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = DesignTokens.touchTargetMin)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = DesignTokens.spacingXs)
    ) {
        Image(
            painter = rememberVectorPainter(icon),
            contentDescription = label,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(DesignTokens.iconSizeLg)
        )
        Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote2,
            color = if (highlighted) AppColors.brand()
            else MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

// ─── 5. Source / Line chips ───

@Composable
private fun SourceChips(
    sources: List<String>,
    current: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm)
    ) {
        Text(
            text = "播放源",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            sources.forEach { name ->
                SelectChip(
                    text = name,
                    selected = name == current,
                    onClick = { onSelect(name) }
                )
            }
        }
    }
}

@Composable
private fun LineChips(
    lines: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm)
    ) {
        Text(
            text = "线路",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            lines.forEachIndexed { index, name ->
                SelectChip(
                    text = name.ifBlank { "线路${index + 1}" },
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

@Composable
private fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .then(
                if (selected) {
                    Modifier
                        .background(AppColors.brand().copy(alpha = 0.12f))
                        .border(1.5.dp, AppColors.brand(), RoundedCornerShape(DesignTokens.radiusMd))
                } else {
                    Modifier.background(MiuixTheme.colorScheme.surfaceVariant)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote1,
            color = if (selected) AppColors.brand() else MiuixTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

// ─── 6. Episode grid ───

@Composable
private fun EpisodeSection(
    episodes: List<Episode>,
    selectedIndex: Int,
    reversed: Boolean,
    onToggleReverse: () -> Unit,
    onExpand: () -> Unit,
    onSelect: (Int, Episode) -> Unit
) {
    val preview = episodes.take(24)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "选集",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (reversed) "正序" else "倒序",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier
                    .clickable(onClick = onToggleReverse)
                    .padding(8.dp)
            )
            Text(
                text = "全 ${episodes.size} 集 >",
                style = MiuixTheme.textStyles.footnote1,
                color = AppColors.brand(),
                modifier = Modifier
                    .clickable(onClick = onExpand)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
        val rows = ((preview.size + 3) / 4).coerceAtLeast(1)
        val gridHeight = (44.dp + DesignTokens.spacingSm) * rows
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
            userScrollEnabled = false
        ) {
            itemsIndexed(preview) { index, episode ->
                EpisodeCell(
                    label = episode.name.ifBlank { "${index + 1}" },
                    selected = index == selectedIndex,
                    onClick = { onSelect(index, episode) }
                )
            }
        }
    }
}

@Composable
private fun EpisodeCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 未播放：纯底无边框；当前播放中：品牌色描边 + 浅底
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .then(
                if (selected) {
                    Modifier.border(
                        1.5.dp,
                        AppColors.brand(),
                        RoundedCornerShape(DesignTokens.radiusMd)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = if (selected) AppColors.brand() else MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Desc sheet ───

@Composable
private fun DescSheet(video: Video, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(250)) { it } + fadeOut(tween(200)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onDismiss)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .heightIn(max = 420.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = DesignTokens.radiusXl,
                            topEnd = DesignTokens.radiusXl
                        )
                    )
                    .background(MiuixTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(DesignTokens.screenPadding)
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            MiuixTheme.colorScheme.outline.copy(alpha = 0.25f),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
                Text(
                    text = "简介",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                Text(
                    text = video.description.ifBlank { "暂无简介" },
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
            }
        }
    }
}

// ─── Episode sheet ───

@Composable
private fun EpisodeSheetOverlay(
    visible: Boolean,
    episodes: List<Episode>,
    selectedEpisodeIndex: Int,
    onDismiss: () -> Unit,
    onSelectEpisode: (Int, Episode) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(250)) { it } + fadeOut(tween(200)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onDismiss)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .heightIn(max = 480.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = DesignTokens.radiusXl,
                            topEnd = DesignTokens.radiusXl
                        )
                    )
                    .background(MiuixTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = DesignTokens.screenPadding,
                                vertical = DesignTokens.spacingMd
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .padding(bottom = DesignTokens.spacingSm)
                                .height(4.dp)
                                .align(Alignment.CenterHorizontally)
                                .background(
                                    MiuixTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "选集 (${episodes.size})",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "收起",
                                style = MiuixTheme.textStyles.footnote1,
                                color = AppColors.brand(),
                                modifier = Modifier
                                    .clickable(onClick = onDismiss)
                                    .padding(8.dp)
                            )
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(horizontal = DesignTokens.screenPadding),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
                    ) {
                        itemsIndexed(episodes) { index, episode ->
                            EpisodeCell(
                                label = episode.name.ifBlank { "${index + 1}" },
                                selected = index == selectedEpisodeIndex,
                                onClick = { onSelectEpisode(index, episode) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
                }
            }
        }
    }
}
