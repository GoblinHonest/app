/*
 * Copyright 2024 Stark Industries
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
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconDownload
import com.stark.miuix.ui.icons.IconHistory
import com.stark.miuix.ui.icons.IconNotice
import com.stark.miuix.ui.icons.IconPaint
import com.stark.miuix.ui.icons.IconRank
import com.stark.miuix.ui.icons.IconSettings
import com.stark.miuix.ui.icons.IconShare
import com.stark.miuix.ui.icons.IconStar
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.TimeUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 我的页面 — CineHub 个人中心
 *
 * 结构：
 * 1. 用户信息区（头像 + 名字 + 标签）
 * 2. 蓝色功能 Banner
 * 3. 四宫格快捷入口
 * 4. 收藏展开列表
 * 5. 继续观看
 * 6. 功能 2×2 网格
 * 7. 菜单列表
 */
@Composable
fun ProfileScreen(
    userDataRepository: UserDataRepository,
    onNavigateToSourceManage: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToSourceRepo: () -> Unit = {}
) {
    val watchHistory by userDataRepository.watchHistory.collectAsState()
    val favorites by userDataRepository.favorites.collectAsState()
    var showFavorites by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        item { UserHeader() }

        item {
            BannerPromo(onNavigateToSourceManage = onNavigateToSourceManage)
        }

        item {
            QuickGrid(
                favoriteCount = favorites.size,
                historyCount = watchHistory.size,
                onFavoriteClick = { showFavorites = !showFavorites },
                onHistoryClick = { /* 历史在下方继续观看区展示 */ },
                onDownloadClick = onNavigateToDownloads
            )
        }

        if (showFavorites && favorites.isNotEmpty()) {
            item {
                FavoritesSection(
                    favorites = favorites,
                    onCollapse = { showFavorites = false },
                    onItemClick = { fav ->
                        onNavigateToDetail(fav.sourceName, fav.detailUrl, fav.title, fav.cover)
                    },
                    onRemove = { fav -> userDataRepository.removeFavorite(fav.videoId) }
                )
            }
        }
        if (showFavorites && favorites.isEmpty()) {
            item {
                Text(
                    text = "暂无收藏，去详情页点亮星星吧",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.outline,
                    modifier = Modifier.padding(
                        horizontal = DesignTokens.screenPadding,
                        vertical = DesignTokens.spacingMd
                    )
                )
            }
        }

        if (watchHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.screenPadding)
                ) {
                    SectionTitle(title = "继续观看")
                    Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                    watchHistory.take(3).forEach { h ->
                        ContinueWatching(
                            item = h,
                            onClick = { item ->
                                onNavigateToDetail(
                                    item.sourceName, item.detailUrl, item.title, item.cover
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
            FeatureGrid(
                onNavigateToSourceManage = onNavigateToSourceManage,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToDownloads = onNavigateToDownloads,
                onNavigateToSourceRepo = onNavigateToSourceRepo
            )
        }

        item {
            Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
            MenuList(
                favorites = favorites,
                onNavigateToSettings = onNavigateToSettings,
                onFavoriteClick = { showFavorites = true }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DesignTokens.brandBlue)
        )
        Spacer(modifier = Modifier.width(DesignTokens.spacingSm))
        Text(
            text = title,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun UserHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DesignTokens.brandBlue.copy(alpha = 0.12f),
                        MiuixTheme.colorScheme.surface
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingMd, bottom = DesignTokens.spacingMd)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                DesignTokens.brandBlueLight,
                                DesignTokens.brandBlue,
                                DesignTokens.brandPurple
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "C",
                    style = MiuixTheme.textStyles.headline1,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CineHub 用户",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(DesignTokens.radiusXs))
                        .background(DesignTokens.brandBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "多源聚合 · 规则驱动",
                        style = MiuixTheme.textStyles.footnote2,
                        color = DesignTokens.brandBlue
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(DesignTokens.touchTargetMin)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberVectorPainter(IconBack),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                    modifier = Modifier
                        .size(DesignTokens.iconSizeSm)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }
    }
}

@Composable
private fun BannerPromo(onNavigateToSourceManage: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .clip(RoundedCornerShape(DesignTokens.radiusLg))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DesignTokens.brandBlue.copy(alpha = 0.18f),
                        DesignTokens.brandPurple.copy(alpha = 0.12f)
                    )
                )
            )
            .padding(horizontal = DesignTokens.spacingLg, vertical = DesignTokens.spacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CineHub · 多源聚合",
                    style = MiuixTheme.textStyles.body2,
                    color = DesignTokens.brandBlue
                )
                Text(
                    text = "导入源，解锁海量视频内容",
                    style = MiuixTheme.textStyles.footnote2,
                    color = DesignTokens.brandBlue.copy(alpha = 0.7f)
                )
            }
            Box(
                modifier = Modifier
                    .heightIn(min = DesignTokens.touchTargetMin)
                    .clip(RoundedCornerShape(DesignTokens.radiusPill))
                    .background(DesignTokens.brandBlue)
                    .clickable(onClick = onNavigateToSourceManage)
                    .padding(horizontal = DesignTokens.spacingLg, vertical = DesignTokens.spacingSm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "管理源",
                    style = MiuixTheme.textStyles.footnote1,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun QuickGrid(
    favoriteCount: Int,
    historyCount: Int,
    onFavoriteClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    data class QuickItem(
        val icon: ImageVector,
        val label: String,
        val badge: String,
        val onClick: () -> Unit
    )
    val items = listOf(
        QuickItem(IconStar, "我的收藏", if (favoriteCount > 0) "$favoriteCount" else "", onFavoriteClick),
        QuickItem(IconHistory, "历史记录", if (historyCount > 0) "$historyCount" else "", onHistoryClick),
        QuickItem(IconDownload, "离线缓存", "", onDownloadClick),
        QuickItem(IconNotice, "我的消息", "", {})
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DesignTokens.spacingXl, bottom = DesignTokens.spacingSm),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = DesignTokens.touchTargetMin)
                    .clickable(onClick = item.onClick)
                    .padding(vertical = DesignTokens.spacingSm)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Image(
                        painter = rememberVectorPainter(item.icon),
                        contentDescription = item.label,
                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                        modifier = Modifier.size(DesignTokens.iconSizeLg)
                    )
                    if (item.badge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DesignTokens.badgeRed)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = item.badge,
                                style = MiuixTheme.textStyles.footnote2,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
                Text(
                    text = item.label,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FavoritesSection(
    favorites: List<Favorite>,
    onCollapse: () -> Unit,
    onItemClick: (Favorite) -> Unit,
    onRemove: (Favorite) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignTokens.spacingSm)
        ) {
            Text(
                text = "我的收藏",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .heightIn(min = DesignTokens.touchTargetMin)
                    .clickable(onClick = onCollapse)
                    .padding(horizontal = DesignTokens.spacingSm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "收起",
                    style = MiuixTheme.textStyles.footnote1,
                    color = DesignTokens.brandBlue
                )
            }
        }
        favorites.forEach { fav ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignTokens.spacingSm)
                    .clickable { onItemClick(fav) },
                cornerRadius = DesignTokens.radiusMd
            ) {
                Row(
                    modifier = Modifier.padding(DesignTokens.spacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverThumb(cover = fav.cover, title = fav.title)
                    Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
                    Text(
                        text = fav.title,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(DesignTokens.touchTargetMin)
                            .clickable { onRemove(fav) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberVectorPainter(IconStar),
                            contentDescription = "取消收藏",
                            colorFilter = ColorFilter.tint(DesignTokens.brandBlue),
                            modifier = Modifier.size(DesignTokens.iconSizeMd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverThumb(cover: String, title: String) {
    Box(
        modifier = Modifier
            .size(width = 72.dp, height = 52.dp)
            .clip(RoundedCornerShape(DesignTokens.radiusSm))
            .background(MiuixTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (cover.isNotBlank() && (cover.startsWith("http://") || cover.startsWith("https://"))) {
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
                color = DesignTokens.brandBlue
            )
        }
    }
}

@Composable
private fun ContinueWatching(item: WatchHistory, onClick: (WatchHistory) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        cornerRadius = DesignTokens.radiusMd
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverThumb(cover = item.cover, title = item.title)
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
                color = DesignTokens.brandBlue
            )
        }
    }
}

@Composable
private fun FeatureGrid(
    onNavigateToSourceManage: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToSourceRepo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
    ) {
        SectionTitle(title = "常用功能")
        Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            FeatureCard(
                icon = IconRank,
                title = "视频源管理",
                subtitle = "添加 / 切换视频源",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSourceManage
            )
            FeatureCard(
                icon = IconDownload,
                title = "下载管理",
                subtitle = "离线缓存视频",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToDownloads
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            FeatureCard(
                icon = IconShare,
                title = "源仓库",
                subtitle = "在线订阅源",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSourceRepo
            )
            FeatureCard(
                icon = IconPaint,
                title = "主题切换",
                subtitle = "亮色 / 暗色模式",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick),
        cornerRadius = DesignTokens.radiusMd
    ) {
        Row(
            modifier = Modifier.padding(DesignTokens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(DesignTokens.radiusSm))
                    .background(DesignTokens.brandBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberVectorPainter(icon),
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(DesignTokens.brandBlue),
                    modifier = Modifier.size(DesignTokens.iconSizeMd)
                )
            }
            Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
            Column {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun MenuList(
    favorites: List<Favorite>,
    onNavigateToSettings: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val groups = listOf(
        listOf(
            MenuItem(IconSettings, "设置", "主题、缓存、语言", onNavigateToSettings)
        ),
        listOf(
            MenuItem(IconStar, "我的收藏", "${favorites.size} 个", onFavoriteClick),
            MenuItem(IconDownload, "检测更新", "当前最新版本", {}),
            MenuItem(IconShare, "关于", "CineHub v1.0.0", {})
        )
    )

    groups.forEach { group ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.screenPadding)
                .padding(bottom = DesignTokens.spacingSm),
            cornerRadius = DesignTokens.radiusMd
        ) {
            Column {
                group.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .padding(horizontal = DesignTokens.spacingLg)
                                .background(MiuixTheme.colorScheme.surfaceVariant)
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
                            colorFilter = ColorFilter.tint(DesignTokens.brandBlue),
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
                        Image(
                            painter = rememberVectorPainter(IconBack),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                            modifier = Modifier
                                .size(DesignTokens.iconSizeSm)
                                .graphicsLayer(rotationZ = 180f)
                        )
                    }
                }
            }
        }
    }
}

private data class MenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)
