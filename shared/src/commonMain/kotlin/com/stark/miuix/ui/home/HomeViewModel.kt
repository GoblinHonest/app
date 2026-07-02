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

import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 首页 UI 状态 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val videos: List<SearchResult>,
        val sources: List<VideoSource>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/**
 * 首页 ViewModel — 多源聚合推荐
 *
 * 并行从所有已启用视频源拉取分类视频，
 * 每个源最多取 [PER_SOURCE_LIMIT] 条，合并后交叉排列。
 * 单个源失败不影响其他源的结果（静默降级）。
 */
class HomeViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** 首次加载 */
    fun loadVideos() {
        scope.launch {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }
            fetchData()
        }
    }

    /** 下拉刷新 — 不切换到 Loading 态 */
    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            fetchData()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchData() {
        try {
            val sources = sourceRepository.getEnabledSources()
            if (sources.isEmpty()) {
                _uiState.value = HomeUiState.Success(videos = emptyList(), sources = emptyList())
                return
            }

            val allVideos = coroutineScope {
                sources.map { source ->
                    async {
                        videoRepository.getCategoryVideos(source)
                            .getOrDefault(emptyList())
                            .take(PER_SOURCE_LIMIT)
                    }
                }.awaitAll()
            }

            val merged = interleave(allVideos)
            _uiState.value = HomeUiState.Success(videos = merged, sources = sources)
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "加载失败")
        }
    }

    /**
     * 交叉合并多个列表
     *
     * 将 [[A1,A2,A3], [B1,B2]] 合并为 [A1,B1,A2,B2,A3]，
     * 让不同源的内容均匀分布，避免单源霸屏。
     */
    private fun interleave(lists: List<List<SearchResult>>): List<SearchResult> {
        val result = mutableListOf<SearchResult>()
        val maxLen = lists.maxOfOrNull { it.size } ?: 0
        for (i in 0 until maxLen) {
            for (list in lists) {
                if (i < list.size) result.add(list[i])
            }
        }
        return result
    }

    companion object {
        private const val PER_SOURCE_LIMIT = 12
    }
}
