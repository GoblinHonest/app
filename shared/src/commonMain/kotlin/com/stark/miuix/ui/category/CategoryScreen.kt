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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoCard
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 分类页 — 带无限滚动分页
 *
 * 滚动到底部倒数第 3 项时自动触发下一页加载，
 * 底部显示加载指示器。
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
    val viewModel = remember(videoRepository, sourceRepository, scope) {
        CategoryViewModel(videoRepository, sourceRepository, scope)
    }
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
                ShimmerVideoGrid()
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
                    val gridState = rememberLazyGridState()

                    // 滚动到底部触发加载更多
                    val shouldLoadMore by remember {
                        derivedStateOf {
                            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val totalItems = gridState.layoutInfo.totalItemsCount
                            lastVisible >= totalItems - 3 && state.hasMore && !state.isLoadingMore
                        }
                    }

                    LaunchedEffect(shouldLoadMore) {
                        if (shouldLoadMore) {
                            viewModel.loadMore(sourceName, categoryUrl)
                        }
                    }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = state.videos,
                            key = { "${it.sourceName}:${it.url}" }
                        ) { video ->
                            VideoCard(
                                searchResult = video,
                                onClick = {
                                    onNavigateToDetail(
                                        video.sourceName, video.url, video.title, video.cover
                                    )
                                }
                            )
                        }

                        // 底部加载指示器
                        if (state.isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
            is CategoryUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.loadCategoryVideos(sourceName, categoryUrl) }
                )
            }
        }
    }
}
