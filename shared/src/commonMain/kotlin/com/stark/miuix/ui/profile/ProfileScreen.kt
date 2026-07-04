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

package com.stark.miuix.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.util.TimeUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 我的页面
 *
 * 展示观看历史（水平滚动卡片）、收藏列表、管理入口。
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

    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
        TopAppBar(title = "我的")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 观看历史
            if (watchHistory.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "最近观看",
                        count = watchHistory.size,
                        actionText = "清空",
                        onAction = { userDataRepository.clearHistory() }
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(watchHistory.take(20), key = { it.videoId }) { history ->
                            HistoryCard(
                                history = history,
                                onClick = {
                                    onNavigateToDetail(
                                        history.sourceName,
                                        history.detailUrl,
                                        history.title,
                                        history.cover
                                    )
                                }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // 收藏列表
            if (favorites.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "我的收藏",
                        count = favorites.size,
                        actionText = "清空",
                        onAction = {
                            favorites.forEach { userDataRepository.removeFavorite(it.videoId) }
                        }
                    )
                }
                items(favorites, key = { it.videoId }) { fav ->
                    FavoriteItem(
                        favorite = fav,
                        onClick = {
                            onNavigateToDetail(
                                fav.sourceName, fav.detailUrl, fav.title, fav.cover
                            )
                        },
                        onRemove = { userDataRepository.removeFavorite(fav.videoId) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // 管理入口
            item {
                Text(
                    text = "管理",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                MenuCard(title = "视频源管理", subtitle = "导入、启用或删除视频源", onClick = onNavigateToSourceManage)
            }
            item {
                MenuCard(title = "设置", subtitle = "主题、播放器、缓存", onClick = onNavigateToSettings)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    cornerRadius = 12.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Miuix 视频聚合 v1.0.0",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Compose Multiplatform + Miuix",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    actionText: String = "",
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (actionText.isNotBlank()) {
            Text(
                text = actionText,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable(onClick = onAction)
            )
        }
        Text(
            text = "$count",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.outline
        )
    }
}

@Composable
private fun HistoryCard(history: WatchHistory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        cornerRadius = 12.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = history.title,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (history.lastEpisode.isNotBlank()) {
                Text(
                    text = history.lastEpisode,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
            Text(
                text = history.sourceName,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.primary,
                maxLines = 1
            )
            if (history.timestamp > 0) {
                Text(
                    text = TimeUtils.formatRelative(history.timestamp),
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteItem(
    favorite: Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showRemoveHint by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showRemoveHint = !showRemoveHint }
            ),
        cornerRadius = 12.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = favorite.title,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = favorite.sourceName,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = ">",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.outline
                )
            }
            if (showRemoveHint) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "长按已选中",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "取消收藏",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier.clickable {
                            onRemove()
                            showRemoveHint = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = ">",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.outline
            )
        }
    }
}
