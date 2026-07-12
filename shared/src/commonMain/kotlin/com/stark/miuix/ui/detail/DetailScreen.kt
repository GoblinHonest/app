/*
 * Copyright 2024 Stark Industries
 *
 * 视频详情页 — 内含小播放器（点全屏原地放大，共用同一个 ExoPlayer）
 */
package com.stark.miuix.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.Video
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconChat
import com.stark.miuix.ui.icons.IconDownload
import com.stark.miuix.ui.icons.IconLike
import com.stark.miuix.ui.icons.IconRank
import com.stark.miuix.ui.icons.IconShare
import com.stark.miuix.ui.icons.IconStar
import com.stark.miuix.ui.player.InlineVideoPlayer
import com.stark.miuix.ui.player.FullscreenControls
import com.stark.miuix.ui.player.releaseSharedPlayer
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.UrlEncoder
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
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
    var selectedEpisodeIndex by remember { mutableStateOf(-1) }
    var resolvedVideoUrl by remember { mutableStateOf<String?>(null) }
    var playerLoading by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var inlinePlayerPosition by remember { mutableLongStateOf(0L) }
    var isPlayerFullscreen by remember { mutableStateOf(false) }
    var isVideoBuffering by remember { mutableStateOf(false) }

    // 拦截返回键：选集弹窗 → 全屏 → 退出详情页
    BackHandler(enabled = true) {
        when {
            episodesExpanded -> episodesExpanded = false
            isPlayerFullscreen -> isPlayerFullscreen = false
            else -> { releaseSharedPlayer(); onNavigateBack() }
        }
    }

    LaunchedEffect(detailUrl) {
        viewModel.loadDetail(sourceName, decodedUrl)
    }

    // 播放器退出全屏时释放资源（大播放器借用的情况）
    LaunchedEffect(isPlayerFullscreen) {
        if (!isPlayerFullscreen) {
            // do nothing - player survives
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isPlayerFullscreen) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 紧凑顶栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Image(painter = rememberVectorPainter(IconBack), contentDescription = "返回",
                            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                            modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { shareAction(initialTitle, "$initialTitle\n$detailUrl") }) {
                        Text("分享", style = MiuixTheme.textStyles.body2)
                    }
                }

                when (val state = uiState) {
                    is DetailUiState.Loading -> {
                        ShimmerVideoGrid(columns = 1, itemCount = 1)
                    }
                    is DetailUiState.Success -> {
                        val video = state.video

                        LaunchedEffect(video) {
                            userDataRepository.addWatchHistory(
                                WatchHistory(videoId = videoId, title = video.title,
                                    cover = video.cover, sourceName = sourceName, detailUrl = decodedUrl)
                            )
                        }

                        val currentUrl = resolvedVideoUrl
                        if (currentUrl != null) {
                            InlineVideoPlayer(
                                url = currentUrl, title = video.title,
                                modifier = Modifier.fillMaxWidth(),
                                onRequestFullscreen = { isPlayerFullscreen = true },
                                isLoading = playerLoading, errorMessage = playerError,
                                onPositionChanged = { pos -> inlinePlayerPosition = pos },
                                isFullscreen = false
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(220.dp)
                                .background(DesignTokens.videoBackground)) {
                                if (video.cover.isNotBlank() &&
                                    (video.cover.startsWith("http://") || video.cover.startsWith("https://"))) {
                                    val ctx = LocalPlatformContext.current
                                    AsyncImage(model = ImageRequest.Builder(ctx).data(video.cover).build(),
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                                Box(modifier = Modifier.fillMaxSize()
                                    .background(Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)), startY = 80f)))
                                Text(text = video.title, style = MiuixTheme.textStyles.headline1, color = Color.White,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
                            }
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item { VideoInfoCard(video) }
                            if (state.allSources.size > 1) {
                                item { SourceSelector(state) { name -> viewModel.switchSource(name) } }
                            }
                            if (video.episodes.isNotEmpty()) {
                                item {
                                    EpisodePreview(video,
                                        selectedEpisodeIndex = selectedEpisodeIndex,
                                        onExpand = { episodesExpanded = true },
                                        onSelect = { index, episode ->
                                            selectedEpisodeIndex = index
                                            playerLoading = true
                                            playerError = null
                                            coroutineScope.launch {
                                                val result = viewModel.getPlayerUrl(state.currentSource, episode.url)
                                                result.fold(
                                                    onSuccess = { streamUrl ->
                                                        resolvedVideoUrl = streamUrl; playerLoading = false
                                                    },
                                                    onFailure = { e ->
                                                        playerLoading = false
                                                        playerError = e.message ?: "播放地址解析失败"
                                                    }
                                                )
                                            }
                                        })
                                }
                            }
                            item { ActionBar(video, userDataRepository, sourceName, videoId, decodedUrl) }
                        }
                    }
                    is DetailUiState.Error -> {
                        ErrorStateView(message = state.message,
                            onRetry = { viewModel.loadDetail(sourceName, decodedUrl) })
                    }
                }
            }
        } else {
            val currentUrl = resolvedVideoUrl
            if (currentUrl != null) {
                val video = (uiState as? DetailUiState.Success)?.video
                // 构建全屏标题：剧名 + 集数（如有）
                val fullscreenTitle = buildString {
                    append(video?.title ?: initialTitle)
                    if (video != null && selectedEpisodeIndex in video.episodes.indices) {
                        val epName = video.episodes[selectedEpisodeIndex].name
                        if (epName.isNotBlank()) append(" - $epName")
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    InlineVideoPlayer(
                        url = currentUrl, title = fullscreenTitle,
                        modifier = Modifier.fillMaxSize(),
                        onRequestFullscreen = {}, isLoading = false, errorMessage = null,
                        onPositionChanged = {}, isFullscreen = true,
                        onBufferingChanged = { isVideoBuffering = it }
                    )
                    FullscreenControls(
                        url = currentUrl, title = fullscreenTitle,
                        onExitFullscreen = { isPlayerFullscreen = false },
                        isBuffering = isVideoBuffering
                    )
                }
            }
        }

        // 选集 Bottom Sheet
        if (!isPlayerFullscreen) {
            val successState = uiState as? DetailUiState.Success
            val sheetVideo = successState?.video
            EpisodeSheetOverlay(
                visible = episodesExpanded && sheetVideo != null,
                video = sheetVideo,
                selectedEpisodeIndex = selectedEpisodeIndex,
                onDismiss = { episodesExpanded = false },
                onSelectEpisode = { index, episode ->
                    selectedEpisodeIndex = index
                    episodesExpanded = false
                    playerLoading = true
                    playerError = null
                    coroutineScope.launch {
                        val src = (uiState as? DetailUiState.Success)?.currentSource ?: ""
                        val result = viewModel.getPlayerUrl(src, episode.url)
                        result.fold(
                            onSuccess = { streamUrl -> resolvedVideoUrl = streamUrl; playerLoading = false },
                            onFailure = { e -> playerLoading = false; playerError = e.message ?: "播放地址解析失败" }
                        )
                    }
                }
            )
        }
    }
}

// ========== Helper Composables (file-level) ==========

@Composable
private fun VideoInfoCard(video: Video) {
    var descExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        cornerRadius = DesignTokens.radiusCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = video.title, style = MiuixTheme.textStyles.headline1,
                color = MiuixTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (video.title.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))
            if (video.status.isNotBlank()) {
                Text(text = video.status, style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (video.description.isNotBlank()) {
                Column {
                    Text(text = video.description, style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = if (descExpanded) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis)
                    val showExpand = video.description.length > 80
                    AnimatedVisibility(
                        visible = showExpand || descExpanded,
                        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
                        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
                    ) {
                        Text(text = if (descExpanded) "收起" else "展开",
                            style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp).clickable { descExpanded = !descExpanded })
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceSelector(state: DetailUiState.Success, onSwitch: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = "播放源 (${state.allSources.size})", style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.allSources.keys.forEach { name ->
                val isSelected = name == state.currentSource
                Box(modifier = Modifier.clip(RoundedCornerShape(DesignTokens.radiusXl))
                    .background(if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant)
                    .clickable { onSwitch(name) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(text = name, style = MiuixTheme.textStyles.body2,
                        color = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun EpisodePreview(video: Video, selectedEpisodeIndex: Int, onExpand: () -> Unit, onSelect: (Int, com.stark.miuix.data.model.Episode) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "选集", style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text(text = "全 ${video.episodes.size} 集", style = MiuixTheme.textStyles.footnote1,
                color = DesignTokens.brandBlue,
                modifier = Modifier.clickable(onClick = onExpand).padding(8.dp))
        }
        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)) {
            video.episodes.take(12).forEachIndexed { index, episode ->
                EpisodeChip(index, episode.name, isSelected = index == selectedEpisodeIndex) { onSelect(index, episode) }
            }
        }
    }
}

@Composable
private fun ActionBar(video: Video, userDataRepository: UserDataRepository, sourceName: String, videoId: String, decodedUrl: String) {
    val shareAction = com.stark.miuix.util.rememberShareAction()
    var isFavorite by remember { mutableStateOf(userDataRepository.isFavorite(videoId)) }
    Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        ActionButton(
            icon = if (isFavorite) IconStar else IconLike,
            label = if (isFavorite) "已收藏" else "收藏",
            tint = if (isFavorite) DesignTokens.brandBlue else MiuixTheme.colorScheme.onSurface
        ) {
            val fav = Favorite(videoId = videoId, title = video.title, cover = video.cover,
                sourceName = sourceName, detailUrl = decodedUrl)
            isFavorite = userDataRepository.toggleFavorite(fav)
        }
        ActionButton(IconChat, "评论") {}
        ActionButton(IconShare, "分享") { shareAction(video.title, "${video.title}\n$decodedUrl") }
        ActionButton(IconDownload, "缓存") {}
        ActionButton(IconRank, "排行") {}
    }
    Spacer(modifier = Modifier.height(DesignTokens.spacingXl))
}

@Composable
private fun EpisodeSheetOverlay(visible: Boolean, video: Video?, selectedEpisodeIndex: Int, onDismiss: () -> Unit, onSelectEpisode: (Int, com.stark.miuix.data.model.Episode) -> Unit) {
    AnimatedVisibility(
        visible = visible && video != null,
        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(250)) { it } + fadeOut(tween(200)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss))
            val density = LocalDensity.current
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .heightIn(max = with(density) { (LocalConfiguration.current.screenHeightDp * 0.65f).dp })
                .clip(RoundedCornerShape(topStart = DesignTokens.radiusXl, topEnd = DesignTokens.radiusXl))
                .background(MiuixTheme.colorScheme.surface).navigationBarsPadding()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingMd)) {
                        Box(modifier = Modifier.width(32.dp).padding(bottom = DesignTokens.spacingSm).height(4.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(2.dp)))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "选集 (${video?.episodes?.size ?: 0})", style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text(text = "收起", style = MiuixTheme.textStyles.footnote1, color = DesignTokens.brandBlue,
                                modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
                        }
                    }
                    val screenWidthDp = LocalConfiguration.current.screenWidthDp
                    val cols = ((screenWidthDp - 32 + 8) / 72).coerceIn(3, 6)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(cols),
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                            .padding(horizontal = DesignTokens.screenPadding),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
                    ) {
                        itemsIndexed(video?.episodes ?: emptyList()) { index, episode ->
                            EpisodeChip(index, episode.name, isSelected = index == selectedEpisodeIndex) {
                                onSelectEpisode(index, episode)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
                }
            }
        }
    }
}

private fun episodesUrl(index: Int, video: Video): String = video.episodes.getOrNull(index)?.url ?: ""

@Composable
private fun EpisodeChip(index: Int, name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier
        .widthIn(min = 52.dp)
        .clip(RoundedCornerShape(DesignTokens.radiusMd))
        .background(if (isSelected) DesignTokens.brandBlue else MiuixTheme.colorScheme.surfaceVariant)
        .clickable(onClick = onClick)
        .padding(horizontal = DesignTokens.spacingSm, vertical = DesignTokens.spacingSm),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name.ifBlank { "${index + 1}" }, style = MiuixTheme.textStyles.footnote1,
            color = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = MiuixTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp).widthIn(min = 44.dp)) {
        androidx.compose.foundation.Image(painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon),
            contentDescription = label, colorFilter = ColorFilter.tint(tint), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.outline)
    }
}
