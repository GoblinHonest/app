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

package com.stark.miuix.ui.category

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.VideoGrid
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.CoroutineScope

/**
 * 分类页
 *
 * 按分类展示视频列表，支持翻页加载更多。
 *
 * @param sourceName 视频源名称
 * @param categoryUrl 分类 URL
 * @param videoRepository 视频仓库
 * @param sourceRepository 视频源仓库
 * @param onNavigateToDetail 导航到详情页
 * @param onNavigateBack 返回上一页
 */
@Composable
fun CategoryScreen(
    sourceName: String,
    categoryUrl: String,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = CategoryViewModel(videoRepository, sourceRepository, scope)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryUrl) {
        viewModel.loadCategoryVideos(sourceName, categoryUrl)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = sourceName,
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("返回", style = MiuixTheme.textStyles.body2)
                }
            }
        )

        when (val state = uiState) {
            is CategoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CategoryUiState.Success -> {
                if (state.videos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "暂无内容",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                } else {
                    VideoGrid(
                        videos = state.videos,
                        onVideoClick = { video ->
                            onNavigateToDetail(video.sourceName, video.url, video.title, video.cover)
                        }
                    )
                }
            }
            is CategoryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
