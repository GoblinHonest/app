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

package com.stark.miuix.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.EpisodeList
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.CoroutineScope

/**
 * 视频详情页
 *
 * 展示视频完整信息：
 * - 顶部返回按钮 + 标题
 * - 封面图片区域
 * - 视频简介
 * - 剧集列表（点击直接播放）
 *
 * @param sourceName 视频源名称
 * @param detailUrl 详情页 URL
 * @param initialTitle 快速展示的初始标题（无需等待加载）
 * @param initialCoverUrl 初始封面（无需等待加载）
 * @param videoRepository 视频仓库
 * @param sourceRepository 视频源仓库
 * @param onNavigateToPlayer 导航到播放页
 * @param onNavigateBack 返回上一页
 */
@Composable
fun DetailScreen(
    sourceName: String,
    detailUrl: String,
    initialTitle: String,
    initialCoverUrl: String,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    onNavigateToPlayer: (String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = DetailViewModel(videoRepository, sourceRepository, scope)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(detailUrl) {
        viewModel.loadDetail(sourceName, detailUrl)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = { Text(initialTitle.ifBlank { "视频详情" }) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("←", style = MiuixTheme.textStyles.title3)
                }
            }
        )

        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Success -> {
                val video = state.video
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // 封面
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            // TODO: 使用 Coil 加载网络图片
                            // AsyncImage(model = video.cover, contentScale = ContentScale.Crop)
                        }
                    }

                    // 视频信息
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = video.title,
                                style = MiuixTheme.textStyles.title1,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                            if (video.status.isNotBlank()) {
                                Text(
                                    text = video.status,
                                    style = MiuixTheme.textStyles.caption,
                                    color = MiuixTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            if (video.description.isNotBlank()) {
                                Text(
                                    text = video.description,
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    // 剧集列表
                    if (video.episodes.isNotEmpty()) {
                        item {
                            Text(
                                text = "剧集列表",
                                style = MiuixTheme.textStyles.title3,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        item {
                            EpisodeList(
                                episodes = video.episodes,
                                onEpisodeClick = { episode ->
                                    onNavigateToPlayer(sourceName, episode.url, video.title)
                                }
                            )
                        }
                    }
                }
            }

            is DetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
private fun rememberCoroutineScope(): CoroutineScope {
    return kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
}
