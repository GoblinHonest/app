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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
 * 我的页面 — 参考优酷 App 设计
 *
 * 布局：
 * 1. 用户头像 + 昵称 + 等级
 * 2. 四格功能图标（收藏/历史/缓存/消息）
 * 3. 继续追番卡片（最近观看）
 * 4. 菜单列表（源管理/设置/关于）
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
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 用户区域
        item {
            UserHeader()
        }

        // 四格功能
        item {
            QuickFunctionGrid(
                favoriteCount = favorites.size,
                historyCount = watchHistory.size
            )
        }

        // 继续观看
        if (watchHistory.isNotEmpty()) {
            item {
                SectionTitle("继续观看")
            }
            item {
                RecentWatchRow(
                    history = watchHistory.take(8),
                    onClick = { onNavigateToDetail(it.sourceName, it.detailUrl, it.title, it.cover) }
                )
            }
        }

        // 我的收藏
        if (favorites.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "我的收藏",
                    action = "全部",
                    onAction = {}
                )
            }
            items(favorites.take(5), key = { it.videoId }) { fav ->
                FavoriteRow(
                    favorite = fav,
                    onClick = { onNavigateToDetail(fav.sourceName, fav.detailUrl, fav.title, fav.cover) },
                    onRemove = { userDataRepository.removeFavorite(fav.videoId) }
                )
            }
        }

        // 工具菜单
        item {
            SectionTitle("工具")
        }
        item {
            MenuSection(
                onNavigateToSourceManage = onNavigateToSourceManage,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

/** 用户头像 + 昵称 */
@Composable
private fun UserHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingLg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "V",
                style = MiuixTheme.textStyles.headline1,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
        Column {
            Text(
                text = "视频爱好者",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface
            )
            Text(
                text = "普通用户",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.outline
            )
        }
    }
}

/** 四格快捷入口（收藏/历史/缓存/消息） */
@Composable
private fun QuickFunctionGrid(favoriteCount: Int, historyCount: Int) {
    val items = listOf(
        Triple("♡", "我的收藏", if (favoriteCount > 0) "$favoriteCount" else ""),
        Triple("◷", "历史记录", if (historyCount > 0) "$historyCount" else ""),
        Triple("↓", "离线缓存", ""),
        Triple("☆", "我的消息", "")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { (icon, label, badge) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = DesignTokens.spacingMd)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(DesignTokens.radiusMd))
                            .background(MiuixTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.primary)
                    }
                    if (badge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE11D48))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = badge, style = MiuixTheme.textStyles.footnote2, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = label, style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String = "", onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (action.isNotBlank()) {
            Text(
                text = action,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

/** 最近观看横向滚动 */
@Composable
private fun RecentWatchRow(history: List<WatchHistory>, onClick: (WatchHistory) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = DesignTokens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingMd)
    ) {
        items(history, key = { it.videoId }) { item ->
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .clickable { onClick(item) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(DesignTokens.radiusSm))
                        .background(MiuixTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.take(2),
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.lastEpisode.isNotBlank()) {
                    Text(
                        text = item.lastEpisode,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteRow(favorite: Favorite, onClick: () -> Unit, onRemove: () -> Unit) {
    var showRemove by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding, vertical = 3.dp)
            .combinedClickable(onClick = onClick, onLongClick = { showRemove = !showRemove }),
        cornerRadius = DesignTokens.radiusMd
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(DesignTokens.radiusSm))
                        .background(MiuixTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(favorite.title.take(1), style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(favorite.title, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(favorite.sourceName, style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.outline)
                }
                Text(">", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.outline)
            }
            if (showRemove) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 72.dp, end = 16.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "取消",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.outline,
                        modifier = Modifier.clickable { showRemove = false }.padding(end = 16.dp)
                    )
                    Text(
                        text = "取消收藏",
                        style = MiuixTheme.textStyles.body2,
                        color = Color(0xFFE11D48),
                        modifier = Modifier.clickable { onRemove(); showRemove = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuSection(onNavigateToSourceManage: () -> Unit, onNavigateToSettings: () -> Unit) {
    val items = listOf(
        Triple("▶", "视频源管理", onNavigateToSourceManage),
        Triple("⚙", "设置", onNavigateToSettings)
    )
    Column(modifier = Modifier.padding(horizontal = DesignTokens.screenPadding)) {
        items.forEachIndexed { index, (icon, label, action) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable(onClick = action),
                cornerRadius = DesignTokens.radiusMd
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icon, style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.primary, modifier = Modifier.width(28.dp))
                    Text(label, style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(">", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.outline)
                }
            }
        }
    }
}
