/*
 * Copyright 2024 Stark Industries
 *
 * 我的页面 — 本地视频聚合 App 功能中心
 * 无登录 / 无消息；渐变顶区 + 数据概览 + 继续观看 + 功能宫格 + 设置列表
 * 风格：HyperOS / Miuix + AppColors / DesignTokens
 */
package com.stark.miuix.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconBook
import com.stark.miuix.ui.icons.IconCast
import com.stark.miuix.ui.icons.IconDownload
import com.stark.miuix.ui.icons.IconPaint
import com.stark.miuix.ui.icons.IconRank
import com.stark.miuix.ui.icons.IconSettings
import com.stark.miuix.ui.icons.IconShare
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.TimeUtils
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 我的页面
 *
 * 结构：
 * 1. 品牌渐变顶区 + 数据概览（收藏 / 历史 / 下载入口）
 * 2. 继续观看横向卡片
 * 3. 常用功能 2×2 宫格（真实跳转）
 * 4. 设置与更多分组列表
 */
@Composable
fun ProfileScreen(
    userDataRepository: UserDataRepository,
    onNavigateToSourceManage: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToSourceRepo: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {}
) {
    val watchHistory by userDataRepository.watchHistory.collectAsState()
    val favorites by userDataRepository.favorites.collectAsState()
    var showFavorites by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // ── 渐变顶区 + 数据概览 ──
        item {
            ProfileHero(
                favoriteCount = favorites.size,
                historyCount = watchHistory.size,
                onFavorites = { showFavorites = !showFavorites; showHistory = false },
                onHistory = { showHistory = !showHistory; showFavorites = false },
                onDownloads = onNavigateToDownloads
            )
        }

        // ── 收藏展开 ──
        item {
            AnimatedVisibility(
                visible = showFavorites,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FavoritesPanel(
                    favorites = favorites,
                    onItemClick = { fav ->
                        onNavigateToDetail(fav.sourceName, fav.detailUrl, fav.title, fav.cover)
                    },
                    onRemove = { fav -> userDataRepository.removeFavorite(fav.videoId) },
                    onCollapse = { showFavorites = false }
                )
            }
        }

        // ── 历史展开 ──
        item {
            AnimatedVisibility(
                visible = showHistory,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                HistoryPanel(
                    history = watchHistory,
                    onItemClick = { h ->
                        onNavigateToDetail(h.sourceName, h.detailUrl, h.title, h.cover)
                    },
                    onCollapse = { showHistory = false }
                )
            }
        }

        // ── 继续观看（横向） ──
        if (watchHistory.isNotEmpty() && !showHistory) {
            item {
                Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
                ContinueWatchingSection(
                    items = watchHistory.take(8),
                    onItemClick = { h ->
                        onNavigateToDetail(h.sourceName, h.detailUrl, h.title, h.cover)
                    },
                    onSeeAll = { showHistory = true; showFavorites = false }
                )
            }
        }

        // ── 常用功能 ──
        item {
            Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
            SectionTitle("常用功能")
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            FeatureGrid(
                onSourceManage = onNavigateToSourceManage,
                onSourceRepo = onNavigateToSourceRepo,
                onDownloads = onNavigateToDownloads,
                onTheme = onNavigateToSettings
            )
        }

        // ── 设置与更多 ──
        item {
            Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
            SectionTitle("设置与更多")
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            SettingsGroup(
                onSettings = onNavigateToSettings,
                onCast = { /* 播放器内投屏 */ },
                onAbout = onNavigateToSettings
            )
        }
    }
}

// ═══════════════════════════════════════
// Hero — 渐变顶区 + 三列数据
// ═══════════════════════════════════════

@Composable
private fun ProfileHero(
    favoriteCount: Int,
    historyCount: Int,
    onFavorites: () -> Unit,
    onHistory: () -> Unit,
    onDownloads: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.brand().copy(alpha = 0.28f),
                        AppColors.brand().copy(alpha = 0.10f),
                        MiuixTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingLg, bottom = DesignTokens.spacingMd)
    ) {
        // 轻量标题行（无 Logo、无登录）
        Text(
            text = "我的",
            style = MiuixTheme.textStyles.headline1,
            color = MiuixTheme.colorScheme.onSurface
        )
        Text(
            text = "本地多源聚合 · 数据保存在本机",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 4.dp, bottom = DesignTokens.spacingLg)
        )

        // 数据概览三列
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DesignTokens.radiusLg))
                .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                .padding(vertical = DesignTokens.spacingMd),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCell(
                value = favoriteCount.toString(),
                label = "收藏",
                accent = Color(0xFFFBBF24),
                onClick = onFavorites
            )
            VerticalDivider()
            StatCell(
                value = historyCount.toString(),
                label = "历史",
                accent = AppColors.brand(),
                onClick = onHistory
            )
            VerticalDivider()
            StatCell(
                value = "↓",
                label = "下载",
                accent = Color(0xFF34D399),
                onClick = onDownloads
            )
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(36.dp)
            .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.25f))
    )
}

@Composable
private fun StatCell(
    value: String,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = DesignTokens.spacingLg, vertical = DesignTokens.spacingXs)
    ) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.title3,
            color = accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

// ═══════════════════════════════════════
// 收藏 / 历史面板
// ═══════════════════════════════════════

@Composable
private fun FavoritesPanel(
    favorites: List<Favorite>,
    onItemClick: (Favorite) -> Unit,
    onRemove: (Favorite) -> Unit,
    onCollapse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingSm)
    ) {
        PanelHeader(title = "我的收藏 (${favorites.size})", onCollapse = onCollapse)
        if (favorites.isEmpty()) {
            EmptyHint("还没有收藏，去详情页点亮星星吧")
        } else {
            favorites.take(12).forEach { fav ->
                MediaRow(
                    cover = fav.cover,
                    title = fav.title,
                    subtitle = fav.sourceName,
                    trailing = "取消",
                    onTrailing = { onRemove(fav) },
                    onClick = { onItemClick(fav) }
                )
                Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            }
        }
    }
}

@Composable
private fun HistoryPanel(
    history: List<WatchHistory>,
    onItemClick: (WatchHistory) -> Unit,
    onCollapse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingSm)
    ) {
        PanelHeader(title = "观看历史 (${history.size})", onCollapse = onCollapse)
        if (history.isEmpty()) {
            EmptyHint("暂无观看记录")
        } else {
            history.take(15).forEach { h ->
                MediaRow(
                    cover = h.cover,
                    title = h.title,
                    subtitle = when {
                        h.lastEpisode.isNotBlank() -> "已看到 ${h.lastEpisode}"
                        h.timestamp > 0 -> TimeUtils.formatRelative(h.timestamp)
                        else -> h.sourceName
                    },
                    trailing = "继续",
                    onTrailing = { onItemClick(h) },
                    onClick = { onItemClick(h) }
                )
                Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            }
        }
    }
}

@Composable
private fun PanelHeader(title: String, onCollapse: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = DesignTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "收起",
            style = MiuixTheme.textStyles.footnote1,
            color = AppColors.brand(),
            modifier = Modifier
                .clickable(onClick = onCollapse)
                .padding(8.dp)
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.body2,
        color = MiuixTheme.colorScheme.outline,
        modifier = Modifier.padding(vertical = DesignTokens.spacingMd)
    )
}

@Composable
private fun MediaRow(
    cover: String,
    title: String,
    subtitle: String,
    trailing: String,
    onTrailing: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(DesignTokens.spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverThumb(cover = cover, title = title)
        Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = trailing,
            style = MiuixTheme.textStyles.footnote1,
            color = AppColors.brand(),
            modifier = Modifier
                .clickable(onClick = onTrailing)
                .padding(8.dp)
        )
    }
}

// ═══════════════════════════════════════
// 继续观看（横向）
// ═══════════════════════════════════════

@Composable
private fun ContinueWatchingSection(
    items: List<WatchHistory>,
    onItemClick: (WatchHistory) -> Unit,
    onSeeAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.screenPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitleInline("继续观看")
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "全部",
                style = MiuixTheme.textStyles.footnote1,
                color = AppColors.brand(),
                modifier = Modifier
                    .clickable(onClick = onSeeAll)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
        LazyRow(
            contentPadding = PaddingValues(horizontal = DesignTokens.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            items(items, key = { it.videoId + it.timestamp }) { item ->
                ContinueCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun ContinueCard(item: WatchHistory, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(DesignTokens.coverAspectRatio)
                .background(MiuixTheme.colorScheme.surfaceContainerHighest)
        ) {
            if (item.cover.isNotBlank() &&
                (item.cover.startsWith("http://") || item.cover.startsWith("https://"))
            ) {
                val ctx = LocalPlatformContext.current
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(item.cover).build(),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )
            if (item.lastEpisode.isNotBlank()) {
                Text(
                    text = item.lastEpisode,
                    style = MiuixTheme.textStyles.footnote2,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                )
            }
        }
        Text(
            text = item.title,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = DesignTokens.spacingSm,
                vertical = DesignTokens.spacingXs
            )
        )
    }
}

// ═══════════════════════════════════════
// 常用功能 2×2
// ═══════════════════════════════════════

@Composable
private fun FeatureGrid(
    onSourceManage: () -> Unit,
    onSourceRepo: () -> Unit,
    onDownloads: () -> Unit,
    onTheme: () -> Unit
) {
    val items = listOf(
        GridItem(IconRank, "视频源", "导入 / 管理源", Color(0xFF5B9BF5), onSourceManage),
        GridItem(IconShare, "源仓库", "在线订阅源", Color(0xFFF59E0B), onSourceRepo),
        GridItem(IconDownload, "下载管理", "离线缓存", Color(0xFF34D399), onDownloads),
        GridItem(IconPaint, "主题外观", "亮色 / 暗色", Color(0xFFA78BFA), onTheme)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
    ) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
            ) {
                row.forEach { item ->
                    FeatureCard(item = item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class GridItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun FeatureCard(item: GridItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(DesignTokens.radiusLg))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = item.onClick)
            .padding(DesignTokens.spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(item.tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberVectorPainter(item.icon),
                contentDescription = item.title,
                colorFilter = ColorFilter.tint(item.tint),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
        Column {
            Text(
                text = item.title,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = item.subtitle,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.outline,
                maxLines = 1
            )
        }
    }
}

// ═══════════════════════════════════════
// 设置分组
// ═══════════════════════════════════════

@Composable
private fun SettingsGroup(
    onSettings: () -> Unit,
    onCast: () -> Unit,
    onAbout: () -> Unit
) {
    val brand = AppColors.brand()
    val rows = listOf(
        ListRow(
            icon = IconSettings,
            tint = brand,
            title = "设置",
            subtitle = "主题、缓存、备份同步",
            onClick = onSettings
        ),
        ListRow(
            icon = IconCast,
            tint = Color(0xFF60A5FA),
            title = "投屏",
            subtitle = "在播放页使用 DLNA",
            onClick = onCast
        ),
        ListRow(
            icon = IconBook,
            tint = Color(0xFFA78BFA),
            title = "关于 CineHub",
            subtitle = "v1.0.0 · 本地多源播放器",
            onClick = onAbout
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .clip(RoundedCornerShape(DesignTokens.radiusLg))
            .background(MiuixTheme.colorScheme.surfaceVariant)
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 56.dp)
                        .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.18f))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(onClick = row.onClick)
                    .padding(
                        horizontal = DesignTokens.spacingLg,
                        vertical = DesignTokens.spacingMd
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(row.tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberVectorPainter(row.icon),
                        contentDescription = row.title,
                        colorFilter = ColorFilter.tint(row.tint),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.title,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    if (row.subtitle.isNotBlank()) {
                        Text(
                            text = row.subtitle,
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.outline
                        )
                    }
                }
                Image(
                    painter = rememberVectorPainter(IconBack),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                    modifier = Modifier
                        .size(14.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }
    }
}

private data class ListRow(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val subtitle: String = "",
    val onClick: () -> Unit
)

// ═══════════════════════════════════════
// 共用小组件
// ═══════════════════════════════════════

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.outline,
        modifier = Modifier.padding(horizontal = DesignTokens.screenPadding)
    )
}

@Composable
private fun SectionTitleInline(title: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.body1,
        color = MiuixTheme.colorScheme.onSurface
    )
}

@Composable
private fun CoverThumb(cover: String, title: String) {
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 68.dp)
            .clip(RoundedCornerShape(DesignTokens.radiusSm))
            .background(MiuixTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (cover.isNotBlank() &&
            (cover.startsWith("http://") || cover.startsWith("https://"))
        ) {
            val ctx = LocalPlatformContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(cover).build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = title.take(1),
                style = MiuixTheme.textStyles.body1,
                color = AppColors.brand()
            )
        }
    }
}
