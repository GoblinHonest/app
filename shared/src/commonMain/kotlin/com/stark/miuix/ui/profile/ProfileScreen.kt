/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconDownload
import com.stark.miuix.ui.icons.IconHistory
import com.stark.miuix.ui.icons.IconLike
import com.stark.miuix.ui.icons.IconNotice
import com.stark.miuix.ui.icons.IconPaint
import com.stark.miuix.ui.icons.IconRank
import com.stark.miuix.ui.icons.IconSettings
import com.stark.miuix.ui.icons.IconShare
import com.stark.miuix.ui.icons.IconStar
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.TimeUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 我的页面 — 参考 Bangumi App 设计
 *
 * 结构（完全对标设计图）：
 * 1. 用户信息区（头像 + 名字 + 等级）
 * 2. 蓝色渐变 Banner 条（功能引导）
 * 3. 四宫格功能图标
 * 4. 「继续追番」卡片（最近观看）
 * 5. 工具 2×2 网格（源管理 / 主题）
 * 6. 菜单列表（设置 / 关于）
 */
@Composable
fun ProfileScreen(
    userDataRepository: UserDataRepository,
    onNavigateToSourceManage: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String, String, String, String) -> Unit
) {
    val watchHistory by userDataRepository.watchHistory.collectAsState()
    val favorites by userDataRepository.favorites.collectAsState()
    var showFavorites by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // 1. 用户信息（内部 UserHeader 处理 statusBarsPadding）
        item {
            UserHeader()
        }

        // 2. 蓝色功能 Banner
        item {
            BannerPromo(onNavigateToSourceManage = onNavigateToSourceManage)
        }

        // 3. 四格功能
        item {
            QuickGrid(
                favoriteCount = favorites.size,
                historyCount = watchHistory.size,
                onFavoriteClick = { showFavorites = !showFavorites },
                onHistoryClick = { /* 历史已在下方显示 */ }
            )
        }

        // 3.5 收藏展开列表
        if (showFavorites && favorites.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = DesignTokens.screenPadding)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = DesignTokens.spacingSm)
                    ) {
                        Text("我的收藏", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text("收起 ∧", style = MiuixTheme.textStyles.footnote1, color = DesignTokens.brandBlue,
                            modifier = Modifier.clickable { showFavorites = false }.padding(8.dp))
                    }
                    favorites.forEach { fav ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = DesignTokens.spacingXs)
                                .clickable { onNavigateToDetail(fav.sourceName, fav.detailUrl, fav.title, fav.cover) },
                            cornerRadius = DesignTokens.radiusMd
                        ) {
                            Row(
                                modifier = Modifier.padding(DesignTokens.spacingMd),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 封面
                                Box(
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 52.dp)
                                        .clip(RoundedCornerShape(DesignTokens.radiusSm))
                                        .background(MiuixTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (fav.cover.startsWith("http")) {
                                        val ctx = LocalPlatformContext.current
                                        AsyncImage(
                                            model = ImageRequest.Builder(ctx).data(fav.cover).build(),
                                            contentDescription = fav.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
                                Text(
                                    text = fav.title,
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                // 取消收藏按钮 — 最小 44dp 触控面积
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { userDataRepository.removeFavorite(fav.videoId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = rememberVectorPainter(IconStar),
                                        contentDescription = "取消收藏",
                                        colorFilter = ColorFilter.tint(DesignTokens.brandBlue),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showFavorites && favorites.isEmpty()) {
            item {
                Text(
                    "暂无收藏",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.outline,
                    modifier = Modifier.padding(
                        horizontal = DesignTokens.screenPadding,
                        vertical = DesignTokens.spacingMd
                    )
                )
            }
        }

        // 4. 继续观看（最近 3 条）
        if (watchHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.screenPadding)
                ) {
                    Text(
                        text = "继续观看",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = DesignTokens.spacingSm)
                    )
                    watchHistory.take(3).forEach { h ->
                        ContinueWatching(
                            item = h,
                            onClick = { item -> onNavigateToDetail(item.sourceName, item.detailUrl, item.title, item.cover) }
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
                    }
                }
            }
        }

        // 5. 功能 2×2 网格
        item {
            Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
            FeatureGrid(
                onNavigateToSourceManage = onNavigateToSourceManage,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        // 6. 菜单列表
        item {
            Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
            MenuList(
                favorites = favorites,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

/** 用户信息区 — 渐变背景头部卡片 */
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
            .statusBarsPadding()  // 沉浸式：渐变延伸到状态栏，内容从状态栏底部开始
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingMd, bottom = DesignTokens.spacingMd)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 头像 — 渐变圆形
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF74B9FF), Color(0xFF0984E3), Color(0xFF6C63FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    style = MiuixTheme.textStyles.headline1,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "视频爱好者",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DesignTokens.brandBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "动漫控",
                            style = MiuixTheme.textStyles.footnote2,
                            color = DesignTokens.brandBlue
                        )
                    }
                }
            }
            androidx.compose.foundation.Image(
                painter = rememberVectorPainter(IconBack),
                contentDescription = "查看资料",
                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

/** 蓝色渐变 Banner 条 */
@Composable
private fun BannerPromo(onNavigateToSourceManage: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(
                Brush.horizontalGradient(
                    listOf(DesignTokens.brandBlue.copy(alpha = 0.18f), Color(0xFF6C63FF).copy(alpha = 0.12f))
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
                    .clip(RoundedCornerShape(DesignTokens.radiusPill))
                    .background(DesignTokens.brandBlue)
                    .clickable(onClick = onNavigateToSourceManage)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
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

/** 四格功能图标（对标设计图 — 线条图标+文字） */
@Composable
private fun QuickGrid(
    favoriteCount: Int,
    historyCount: Int,
    onFavoriteClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    // 逆向图标: star(收藏) / time(历史) / download(缓存) / notice(消息)
    data class QuickItem(val icon: ImageVector, val label: String, val badge: String, val onClick: () -> Unit)
    val items = listOf(
        QuickItem(IconStar, "我的收藏", if (favoriteCount > 0) "$favoriteCount" else "", onFavoriteClick),
        QuickItem(IconHistory, "历史记录", if (historyCount > 0) "$historyCount" else "", onHistoryClick),
        QuickItem(IconDownload, "离线缓存", "", {}),
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
                    .clickable(onClick = item.onClick)
                    .padding(vertical = DesignTokens.spacingSm)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    // SVG 路径图标（对标逆向 APK 图标集）
                    androidx.compose.foundation.Image(
                        painter = rememberVectorPainter(item.icon),
                        contentDescription = item.label,
                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .size(26.dp)
                            .padding(2.dp)
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

/** 继续追番卡片 — 对标设计图的「继续追番」条目 */
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
            // 封面缩略图 — 使用 AsyncImage 显示真实封面
            val coverUrl = item.cover
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 52.dp)
                    .clip(RoundedCornerShape(DesignTokens.radiusSm))
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (coverUrl.isNotBlank() && (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))) {
                    val ctx = LocalPlatformContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(coverUrl).build(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = item.title.take(1),
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.primary
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
                if (item.lastEpisode.isNotBlank()) {
                    Text(
                        text = "已看到${item.lastEpisode}",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.outline
                    )
                } else if (item.timestamp > 0) {
                    Text(
                        text = TimeUtils.formatRelative(item.timestamp),
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
            }
            Text(
                text = "继续观看 >",
                style = MiuixTheme.textStyles.footnote1,
                color = DesignTokens.brandBlue
            )
        }
    }
}

/** 2×2 功能入口（对标设计图的 入站问答/主题切换 小卡片） */
@Composable
private fun FeatureGrid(
    onNavigateToSourceManage: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // 2×2 功能网格 — 只保留视频源管理和主题切换
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
    ) {
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
                icon = IconPaint,
                title = "主题切换",
                subtitle = "选择亮色或暗色",
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
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = DesignTokens.radiusMd
    ) {
        Row(
            modifier = Modifier.padding(DesignTokens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = rememberVectorPainter(icon),
                contentDescription = title,
                colorFilter = ColorFilter.tint(DesignTokens.brandBlue),
                modifier = Modifier.size(24.dp)
            )
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

/** 菜单列表（对标设计图：图标+主文字+箭头，白色圆角卡片分组） */
@Composable
private fun MenuList(
    favorites: List<Favorite>,
    onNavigateToSettings: () -> Unit
) {
    val groups = listOf(
        listOf(
            // 逆向图标: settings/star/download/share
            MenuItem(IconSettings, "设置", "主题、缓存、语言", onNavigateToSettings),
        ),
        listOf(
            MenuItem(IconStar, "我的收藏", "${favorites.size} 个", {}),
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
                            .clickable(onClick = item.onClick)
                            .padding(
                                horizontal = DesignTokens.spacingLg,
                                vertical = DesignTokens.spacingMd
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SVG 路径图标（逆向 APK 图标集）
                        androidx.compose.foundation.Image(
                            painter = rememberVectorPainter(item.icon),
                            contentDescription = item.title,
                            colorFilter = ColorFilter.tint(DesignTokens.brandBlue),
                            modifier = Modifier.size(22.dp)
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
                        // chevron 箭头 — SVG IconBack 旋转 180°
                        androidx.compose.foundation.Image(
                            painter = rememberVectorPainter(IconBack),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                            modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 180f)
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
