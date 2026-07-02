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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.detail.DetailViewModel
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.CoroutineScope

/**
 * 播放页
 *
 * 全屏视频播放器页面。
 *
 * @param sourceName 视频源名称
 * @param episodeUrl 剧集 URL
 * @param title 视频标题
 * @param videoRepository 视频仓库
 * @param sourceRepository 视频源仓库
 * @param onNavigateBack 返回上一页
 */
@Composable
fun PlayerScreen(
    sourceName: String,
    episodeUrl: String,
    title: String,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = DetailViewModel(videoRepository, sourceRepository, scope)
    var videoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(episodeUrl) {
        val result = viewModel.getPlayerUrl(sourceName, episodeUrl)
        result.fold(
            onSuccess = {
                videoUrl = it
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message ?: "播放地址解析失败"
                isLoading = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = title.ifBlank { "播放" },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("返回", style = MiuixTheme.textStyles.body2)
                }
            }
        )

        // 播放器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) {
                Text(
                    text = "解析播放地址中...",
                    modifier = Modifier.fillMaxSize(),
                    style = MiuixTheme.textStyles.body1
                )
            } else if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxSize(),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.error
                )
            } else {
                VideoPlayer(
                    url = videoUrl,
                    title = title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
