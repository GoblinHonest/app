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

package com.stark.miuix.ui.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.VideoGrid
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.SusIcon
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SquircleShape

/**
 * 首页
 *
 * 应用的主入口页面，展示：
 * - 顶部 TopAppBar（标题 + 搜索按钮 + 设置按钮）
 * - 视频源状态概览
 * - 推荐视频网格列表
 * - 空状态提示（无已启用的视频源时）
 *
 * @param videoRepository 视频仓库
 * @param sourceRepository 视频源仓库
 * @param onNavigateToSearch 导航到搜索页
 * @param onNavigateToCategory 导航到分类页
 * @param onNavigateToDetail 导航到详情页
 * @param onNavigateToSourceManage 导航到视频源管理
 * @param onNavigateToSettings 导航到设置页
 */
@Composable
fun HomeScreen(
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    onNavigateToSearch: () -> Unit,
    onNavigateToCategory: (String, String) -> Unit,
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateToSourceManage: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel = HomeViewModel(videoRepository, sourceRepository, rememberCoroutineScope())
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVideos()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = { Text("视频聚合") },
            actions = {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(
                        imageVector = SusIcon,
                        contentDescription = "搜索"
                    )
                }
                IconButton(onClick = onNavigateToSourceManage) {
                    Text("源", style = MiuixTheme.textStyles.body1)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Text("⚙", style = MiuixTheme.textStyles.body1)
                }
            }
        )

        // 内容区域
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeUiState.Success -> {
                if (state.sources.isEmpty()) {
                    // 空状态
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "暂无视频源",
                                style = MiuixTheme.textStyles.title3,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "请先导入视频源",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Card(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .clickable { onNavigateToSourceManage() },
                                shape = SquircleShape(12.dp)
                            ) {
                                Text(
                                    text = "导入视频源",
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    style = MiuixTheme.textStyles.body1,
                                    color = MiuixTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // 视频源概览卡片
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = SquircleShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "已启用 ${state.sources.size} 个视频源",
                                            style = MiuixTheme.textStyles.title3,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "管理 >",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { onNavigateToSourceManage() }
                                    )
                                }
                            }
                        }

                        // 推荐视频标题
                        if (state.videos.isNotEmpty()) {
                            item {
                                Text(
                                    text = "推荐视频",
                                    style = MiuixTheme.textStyles.title2,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // 视频网格
                        item {
                            VideoGrid(
                                videos = state.videos,
                                onVideoClick = { video ->
                                    onNavigateToDetail(
                                        video.sourceName,
                                        video.url,
                                        video.title,
                                        video.cover
                                    )
                                }
                            )
                        }
                    }
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "加载失败",
                            style = MiuixTheme.textStyles.title3,
                            color = MiuixTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberCoroutineScope(): kotlinx.coroutines.CoroutineScope {
    return kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
}
