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

package com.stark.miuix.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.util.UrlEncoder
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.detail.DetailViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 播放页 — 全屏视频播放
 *
 * 三阶段状态：解析中 → 播放 / 错误。
 * 播放成功后自动更新 [UserDataRepository] 中的观看进度。
 */
@Composable
fun PlayerScreen(
    sourceName: String,
    episodeUrl: String,
    title: String,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    userDataRepository: UserDataRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember(videoRepository, sourceRepository, scope) {
        DetailViewModel(videoRepository, sourceRepository, scope)
    }
    val decodedEpisodeUrl = UrlEncoder.decode(episodeUrl)
    var videoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(decodedEpisodeUrl) {
        isLoading = true
        errorMessage = ""
        val result = viewModel.getPlayerUrl(sourceName, decodedEpisodeUrl)
        result.fold(
            onSuccess = {
                videoUrl = it
                isLoading = false
                userDataRepository.addWatchHistory(
                    WatchHistory(
                        videoId = episodeUrl.hashCode().toString(),
                        title = title,
                        cover = "",
                        sourceName = sourceName,
                        detailUrl = "",
                        lastEpisode = title
                    )
                )
            },
            onFailure = { errorMessage = it.message ?: "播放地址解析失败"; isLoading = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = title.ifBlank { "播放" },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("返回", style = MiuixTheme.textStyles.body2)
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在解析播放地址...",
                            style = MiuixTheme.textStyles.body2,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                errorMessage.isNotBlank() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage,
                            style = MiuixTheme.textStyles.body1,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.clickable {
                                isLoading = true
                                errorMessage = ""
                                scope.launch {
                                    val result = viewModel.getPlayerUrl(sourceName, decodedEpisodeUrl)
                                    result.fold(
                                        onSuccess = { videoUrl = it; isLoading = false },
                                        onFailure = { errorMessage = it.message ?: "解析失败"; isLoading = false }
                                    )
                                }
                            },
                            cornerRadius = 10.dp
                        ) {
                            Text(
                                text = "重试",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }
                else -> {
                    VideoPlayer(
                        url = videoUrl,
                        title = title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
