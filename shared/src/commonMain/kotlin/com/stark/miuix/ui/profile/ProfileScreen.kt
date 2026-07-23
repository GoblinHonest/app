/*
 * Copyright 2024 Stark Industries
 *
 * 我的页面 — 本地视频 App 设置中心（无需登录）
 * 布局参考：App 标题 + 版本 → 常用功能 2 列网格 → 分组列表
 * 风格：Miuix + AppColors / DesignTokens
 */
package com.stark.miuix.ui.profile

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconBook
import com.stark.miuix.ui.icons.IconCast
import com.stark.miuix.ui.icons.IconDownload
import com.stark.miuix.ui.icons.IconHistory
import com.stark.miuix.ui.icons.IconPaint
import com.stark.miuix.ui.icons.IconPlay
import com.stark.miuix.ui.icons.IconRank
import com.stark.miuix.ui.icons.IconSearch
import com.stark.miuix.ui.icons.IconSettings
import com.stark.miuix.ui.icons.IconShare
import com.stark.miuix.ui.icons.IconStar
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.TimeUtils
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val APP_NAME = "CineHub"
private const val APP_VERSION = "v1.0.0"

/**
 * 我的页面 — 本地功能入口
 *
 * 1. App 标题 + 版本 + Logo（无登录）
 * 2. 常用功能 2×3 网格
 * 3. 继续观看（有历史时）
 * 4. 设备互联列表
 * 5. 设置与更多列表
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item { AppHeader() }

        item {
            SectionLabel("常用功能")
            FeatureGrid(
                favoriteCount = favorites.size,
                onSourceManage = onNavigateToSourceManage,
                onSourceRepo = onNavigateToSourceRepo,
                onSearch = onNavigateToSearch,
                onDownloads = onNavigateToDownloads,
                onSettings = onNavigateToSettings,
                onFavorites = onNavigateToSettings
            )
        }

        if (watchHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.screenPadding)
                ) {
                    SectionLabelInline("继续观看")
                    Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                    watchHistory.take(4).forEach { h ->
                        ContinueWatchingCard(
                            item = h,
                            onClick = {
                                onNavigateToDetail(
                                    h.sourceName, h.detailUrl, h.title, h.cover
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
            SectionLabel("设备互联")
            GroupedList(
                items = listOf(
                    ListRowData(
                        icon = IconCast,
                        iconTint = Color(0xFF60A5FA),
                        title = "投屏 / 电视控制",
                        subtitle = "DLNA 投屏到电视",
                        onClick = { }
                    )
                )
            )
        }

        item {
            val brand = AppColors.brand()
            val muted = MiuixTheme.colorScheme.onSurfaceVariantSummary
            Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
            SectionLabel("设置与更多")
            GroupedList(
                items = listOf(
                    ListRowData(
                        icon = IconSettings,
                        iconTint = muted,
                        title = "设置",
                        subtitle = "主题、缓存、备份",
                        onClick = onNavigateToSettings
                    ),
                    ListRowData(
                        icon = IconStar,
                        iconTint = Color(0xFFFBBF24),
                        title = "我的收藏",
                        trailing = if (favorites.isNotEmpty()) "${favorites.size}" else null,
                        onClick = onNavigateToSettings
                    ),
                    ListRowData(
                        icon = IconHistory,
                        iconTint = Color(0xFF34D399),
                        title = "观看历史",
                        trailing = if (watchHistory.isNotEmpty()) "${watchHistory.size}" else null,
                        onClick = { }
                    ),
                    ListRowData(
                        icon = IconBook,
                        iconTint = brand,
                        title = "关于",
                        trailing = APP_VERSION,
                        onClick = onNavigateToSettings
                    )
                )
            )
        }
    }
}

// ─── Header ───

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingXl, bottom = DesignTokens.spacingLg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = APP_NAME,
                style = MiuixTheme.textStyles.headline1,
                color = MiuixTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = APP_VERSION,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.outline
            )
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(DesignTokens.radiusLg))
                .background(
                    Brush.linearGradient(
                        listOf(
                            AppColors.brand(),
                            DesignTokens.brandPurple
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberVectorPainter(IconPlay),
                contentDescription = APP_NAME,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ─── Section labels ───

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.outline,
        modifier = Modifier.padding(
            horizontal = DesignTokens.screenPadding,
            vertical = DesignTokens.spacingSm
        )
    )
}

@Composable
private fun SectionLabelInline(title: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.outline
    )
}

// ─── Feature 2-column grid ───

@Composable
private fun FeatureGrid(
    favoriteCount: Int,
    onSourceManage: () -> Unit,
    onSourceRepo: () -> Unit,
    onSearch: () -> Unit,
    onDownloads: () -> Unit,
    onSettings: () -> Unit,
    onFavorites: () -> Unit
) {
    val items = listOf(
        GridItem(IconRank, "视频源管理", Color(0xFF5B9BF5), onSourceManage),
        GridItem(IconShare, "源仓库", Color(0xFFF59E0B), onSourceRepo),
        GridItem(IconDownload, "下载管理", Color(0xFF34D399), onDownloads),
        GridItem(IconPaint, "主题模式", Color(0xFFA78BFA), onSettings),
        GridItem(IconSearch, "搜索", Color(0xFFFBBF24), onSearch),
        GridItem(
            IconStar,
            if (favoriteCount > 0) "收藏 ($favoriteCount)" else "我的收藏",
            Color(0xFFF87171),
            onFavorites
        )
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
                    FeatureCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class GridItem(
    val icon: ImageVector,
    val title: String,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun FeatureCard(
    item: GridItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(DesignTokens.radiusLg))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = item.onClick)
            .padding(horizontal = DesignTokens.spacingMd, vertical = DesignTokens.spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(item.tint.copy(alpha = 0.18f)),
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
        Text(
            text = item.title,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Grouped list ───

private data class ListRowData(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val subtitle: String = "",
    val trailing: String? = null,
    val onClick: () -> Unit
)

@Composable
private fun GroupedList(items: List<ListRowData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .clip(RoundedCornerShape(DesignTokens.radiusLg))
            .background(MiuixTheme.colorScheme.surfaceVariant)
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 56.dp)
                        .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = DesignTokens.touchTargetMin)
                    .clickable(onClick = item.onClick)
                    .padding(
                        horizontal = DesignTokens.spacingLg,
                        vertical = DesignTokens.spacingMd
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberVectorPainter(item.icon),
                    contentDescription = item.title,
                    colorFilter = ColorFilter.tint(item.iconTint),
                    modifier = Modifier.size(DesignTokens.iconSizeMd)
                )
                Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.outline
                        )
                    }
                }
                if (!item.trailing.isNullOrBlank()) {
                    Text(
                        text = item.trailing,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Image(
                    painter = rememberVectorPainter(IconBack),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }
    }
}

// ─── Continue watching ───

@Composable
private fun ContinueWatchingCard(
    item: WatchHistory,
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
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 52.dp)
                .clip(RoundedCornerShape(DesignTokens.radiusSm))
                .background(MiuixTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
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
            } else {
                Text(
                    text = item.title.take(1),
                    style = MiuixTheme.textStyles.body1,
                    color = AppColors.brand()
                )
            }
        }
        Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when {
                    item.lastEpisode.isNotBlank() -> "已看到${item.lastEpisode}"
                    item.timestamp > 0 -> TimeUtils.formatRelative(item.timestamp)
                    else -> "继续观看"
                },
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.outline
            )
        }
        Text(
            text = "继续 >",
            style = MiuixTheme.textStyles.footnote1,
            color = AppColors.brand()
        )
    }
}
