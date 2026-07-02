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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.EpisodeList
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.util.ClipboardUtils
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频详情页
 *
 * 沉浸式封面 + 渐变蒙版 + 收藏按钮 + 剧集列表。
 * 进入详情页时自动记录到观看历史。
 */
@Composable
fun DetailScreen(
    sourceName: String,
    detailUrl: String,
    initialTitle: String,
    initialCoverUrl: String,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    userDataRepository: UserDataRepository,
    onNavigateToPlayer: (String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember(videoRepository, sourceRepository, scope) {
        DetailViewModel(videoRepository, sourceRepository, scope)
    }
    val uiState by viewModel.uiState.collectAsState()
    val videoId = detailUrl.hashCode().toString()
    var isFavorite by remember { mutableStateOf(userDataRepository.isFavorite(videoId)) }
    val copyToClipboard = ClipboardUtils.rememberCopyAction()
    var descExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(detailUrl) {
        viewModel.loadDetail(sourceName, detailUrl)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = initialTitle.ifBlank { "视频详情" },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("返回", style = MiuixTheme.textStyles.body2)
                }
            },
            actions = {
                IconButton(onClick = {
                    copyToClipboard("$initialTitle\n$detailUrl")
                }) {
                    Text("分享", style = MiuixTheme.textStyles.body2)
                }
                IconButton(onClick = {
                    val fav = Favorite(
                        videoId = videoId,
                        title = initialTitle,
                        cover = initialCoverUrl,
                        sourceName = sourceName,
                        detailUrl = detailUrl
                    )
                    isFavorite = userDataRepository.toggleFavorite(fav)
                }) {
                    Text(
                        text = if (isFavorite) "已收藏" else "收藏",
                        style = MiuixTheme.textStyles.body2,
                        color = if (isFavorite) MiuixTheme.colorScheme.primary
                               else MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        )

        when (val state = uiState) {
            is DetailUiState.Loading -> {
                ShimmerVideoGrid(columns = 1, itemCount = 1)
            }

            is DetailUiState.Success -> {
                val video = state.video

                // 加载成功后记录观看历史
                LaunchedEffect(video) {
                    userDataRepository.addWatchHistory(
                        WatchHistory(
                            videoId = videoId,
                            title = video.title,
                            cover = video.cover,
                            sourceName = sourceName,
                            detailUrl = detailUrl
                        )
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // 封面 + 渐变蒙版
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Color(0xFF1A1A1A))
                        ) {
                            if (video.cover.isNotBlank()) {
                                val painterResource = asyncPainterResource(video.cover)
                                KamelImage(
                                    resource = painterResource,
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onFailure = { }
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.6f)
                                            ),
                                            startY = 100f
                                        )
                                    )
                            )
                            Text(
                                text = video.title,
                                style = MiuixTheme.textStyles.headline1,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            )
                        }
                    }

                    // 视频信息卡片
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            cornerRadius = 12.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (video.status.isNotBlank()) {
                                    Text(
                                        text = video.status,
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (video.description.isNotBlank()) {
                                    Text(
                                        text = video.description,
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        maxLines = if (descExpanded) Int.MAX_VALUE else 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (descExpanded) "收起" else "展开",
                                        style = MiuixTheme.textStyles.footnote1,
                                        color = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable { descExpanded = !descExpanded }
                                    )
                                }
                                Text(
                                    text = "来源: $sourceName",
                                    style = MiuixTheme.textStyles.footnote2,
                                    color = MiuixTheme.colorScheme.outline,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    // 剧集列表
                    if (video.episodes.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "剧集 (${video.episodes.size})",
                                    style = MiuixTheme.textStyles.body1,
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                            }
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
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.loadDetail(sourceName, detailUrl) }
                )
            }
        }
    }
}
