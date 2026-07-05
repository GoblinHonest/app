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
import com.stark.miuix.ui.icons.IconDownload
import com.stark.miuix.ui.icons.IconHistory
import com.stark.miuix.ui.icons.IconLike
import com.stark.miuix.ui.icons.IconNotice
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // 1. 用户信息
        item {
            UserHeader()
        }

        // 2. 蓝色功能 Banner
        item {
            BannerPromo()
        }

        // 3. 四格功能
        item {
            QuickGrid(
                favoriteCount = favorites.size,
                historyCount = watchHistory.size
            )
        }

        // 4. 继续观看（最近一条）
        if (watchHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(DesignTokens.spacingMd))
                ContinueWatching(
                    item = watchHistory.first(),
                    onClick = { h ->
                        onNavigateToDetail(h.sourceName, h.detailUrl, h.title, h.cover)
                    }
                )
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

/** 用户信息区 — 白色背景卡片 */
@Composable
private fun UserHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface)
            .padding(
                horizontal = DesignTokens.screenPadding,
                vertical = DesignTokens.spacingXl
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF74B9FF), Color(0xFF0984E3))
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "普通用户",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.outline
            )
        }
        Text(
            text = ">",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.outline
        )
    }
}

/** 蓝色渐变 Banner 条 */
@Composable
private fun BannerPromo() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF74B9FF).copy(alpha = 0.3f), Color(0xFF0984E3).copy(alpha = 0.25f))
                )
            )
            .padding(horizontal = DesignTokens.spacingLg, vertical = DesignTokens.spacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "多源聚合，畅享海量视频内容",
                style = MiuixTheme.textStyles.body2,
                color = DesignTokens.brandBlue,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusPill))
                    .background(DesignTokens.brandBlue)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "导入源",
                    style = MiuixTheme.textStyles.footnote1,
                    color = Color.White
                )
            }
        }
    }
}

/** 四格功能图标（对标设计图 — 线条图标+文字） */
@Composable
private fun QuickGrid(favoriteCount: Int, historyCount: Int) {
    // 逆向图标: star(收藏) / time(历史) / download(缓存) / notice(消息)
    val items = listOf(
        Triple(IconStar, "我的收藏", if (favoriteCount > 0) "$favoriteCount" else ""),
        Triple(IconHistory, "历史记录", if (historyCount > 0) "$historyCount" else ""),
        Triple(IconDownload, "离线缓存", ""),
        Triple(IconNotice, "我的消息", "")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DesignTokens.spacingXl, bottom = DesignTokens.spacingSm),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { (icon, label, badge) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(72.dp)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    // SVG 路径图标（对标逆向 APK 图标集）
                    androidx.compose.foundation.Image(
                        painter = rememberVectorPainter(icon),
                        contentDescription = label,
                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .size(26.dp)
                            .padding(2.dp)
                    )
                    if (badge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DesignTokens.badgeRed)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MiuixTheme.textStyles.footnote2,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
                Text(
                    text = label,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private data class QuickItem(val icon: ImageVector, val label: String, val badge: String)

/** 继续追番卡片 — 对标设计图的「继续追番」条目 */
@Composable
private fun ContinueWatching(item: WatchHistory, onClick: (WatchHistory) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .clickable { onClick(item) },
        cornerRadius = DesignTokens.radiusMd
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面缩略图
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 40.dp)
                    .clip(RoundedCornerShape(DesignTokens.radiusSm))
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.take(1),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.primary
                )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding),
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
            icon = IconSettings,
            title = "主题切换",
            subtitle = "选择亮色或暗色",
            modifier = Modifier.weight(1f),
            onClick = onNavigateToSettings
        )
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
                modifier = Modifier.size(20.dp).width(28.dp)
            )
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
            MenuItem(IconShare, "关于", "Miuix 视频聚合 v1.0.0", {})
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
                            modifier = Modifier
                                .size(22.dp)
                                .width(32.dp)
                        )
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
                        Text(
                            text = ">",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.outline
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
