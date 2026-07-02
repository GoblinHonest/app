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

import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 分类页 UI 状态 */
sealed interface CategoryUiState {
    data object Loading : CategoryUiState
    data class Success(
        val videos: List<SearchResult>,
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false
    ) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}

/**
 * 分类页 ViewModel
 *
 * 支持分页加载：首次加载显示骨架屏，
 * 滚动到底部时追加下一页数据到已有列表。
 */
class CategoryViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLoadingMore = false
    private val allVideos = mutableListOf<SearchResult>()

    /** 首次加载 — 清空已有数据，从第 1 页开始 */
    fun loadCategoryVideos(sourceName: String, categoryUrl: String) {
        coroutineScope.launch {
            currentPage = 1
            allVideos.clear()
            _uiState.value = CategoryUiState.Loading

            val source = sourceRepository.getSourceByName(sourceName)
            if (source == null) {
                _uiState.value = CategoryUiState.Error("视频源不存在")
                return@launch
            }

            val result = videoRepository.getCategoryVideos(source, categoryUrl, 1)
            result.fold(
                onSuccess = { videos ->
                    allVideos.addAll(videos)
                    _uiState.value = CategoryUiState.Success(
                        videos = allVideos.toList(),
                        hasMore = videos.size >= PAGE_SIZE
                    )
                },
                onFailure = { e ->
                    _uiState.value = CategoryUiState.Error(e.message ?: "加载失败")
                }
            )
        }
    }

    /** 加载下一页 — 追加到列表末尾 */
    fun loadMore(sourceName: String, categoryUrl: String) {
        if (isLoadingMore) return
        val current = _uiState.value
        if (current !is CategoryUiState.Success || !current.hasMore) return

        isLoadingMore = true
        _uiState.value = current.copy(isLoadingMore = true)

        coroutineScope.launch {
            val source = sourceRepository.getSourceByName(sourceName) ?: return@launch
            val nextPage = currentPage + 1
            val result = videoRepository.getCategoryVideos(source, categoryUrl, nextPage)

            result.fold(
                onSuccess = { videos ->
                    currentPage = nextPage
                    allVideos.addAll(videos)
                    _uiState.value = CategoryUiState.Success(
                        videos = allVideos.toList(),
                        hasMore = videos.size >= PAGE_SIZE
                    )
                },
                onFailure = {
                    _uiState.value = current.copy(isLoadingMore = false)
                }
            )
            isLoadingMore = false
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
