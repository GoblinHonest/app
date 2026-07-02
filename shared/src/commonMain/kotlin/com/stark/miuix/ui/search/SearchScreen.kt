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

package com.stark.miuix.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.VideoGrid
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SquircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.basic.Card
import kotlinx.coroutines.CoroutineScope

/**
 * 搜索页
 *
 * 功能：
 * - 顶部搜索栏（支持回车搜索）
 * - 搜索历史（FlowRow 标签展示）
 * - 搜索结果网格
 * - 加载状态和错误提示
 *
 * @param videoRepository 视频仓库
 * @param sourceRepository 视频源仓库
 * @param onNavigateToDetail 导航到详情页
 * @param onNavigateBack 返回上一页
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = SearchViewModel(videoRepository, scope)
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()

    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏
        TopAppBar(
            title = {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "搜索视频..."
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("←", style = MiuixTheme.textStyles.title3)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.search(query) }) {
                    Text("搜索", style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.primary)
                }
            }
        )

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                // 搜索历史
                if (history.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = "搜索历史",
                                style = MiuixTheme.textStyles.title3,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }
                        items(history) { keyword ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    .clickable {
                                        query = keyword
                                        viewModel.search(keyword)
                                    },
                                shape = SquircleShape(8.dp)
                            ) {
                                Text(
                                    text = keyword,
                                    modifier = Modifier.padding(12.dp),
                                    style = MiuixTheme.textStyles.body1,
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            is SearchUiState.Searching -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is SearchUiState.Success -> {
                VideoGrid(
                    videos = state.results,
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

            is SearchUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("搜索失败", style = MiuixTheme.textStyles.title3,
                            color = MiuixTheme.colorScheme.error)
                        Text(state.message, style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp))
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
