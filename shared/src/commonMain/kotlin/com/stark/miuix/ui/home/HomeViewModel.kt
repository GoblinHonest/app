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
import com.stark.miuix.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

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
 * 使用 [supervisorScope] 并行加载，单个源失败不影响其他源。
 */
class HomeViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadVideos() {
        scope.launch {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }
            fetchData()
        }
    }

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
            AppLogger.d("Home", "加载首页, 启用源数: ${sources.size}")

            if (sources.isEmpty()) {
                _uiState.value = HomeUiState.Success(videos = emptyList(), sources = emptyList())
                return
            }

            // supervisorScope: 单个源失败不取消其他源
            val allVideos = supervisorScope {
                sources.map { source ->
                    async {
                        try {
                            val result = videoRepository.getCategoryVideos(source)
                            val videos = result.getOrDefault(emptyList()).take(PER_SOURCE_LIMIT)
                            AppLogger.d("Home", "源[${source.sourceName}] 加载 ${videos.size} 条")
                            videos
                        } catch (e: Exception) {
                            AppLogger.e("Home", "源[${source.sourceName}] 加载失败", e)
                            emptyList()
                        }
                    }
                }.awaitAll()
            }

            val merged = interleave(allVideos)
            AppLogger.d("Home", "聚合完成, 总计 ${merged.size} 条视频")
            _uiState.value = HomeUiState.Success(videos = merged, sources = sources)
        } catch (e: Exception) {
            AppLogger.e("Home", "首页加载异常", e)
            _uiState.value = HomeUiState.Error(e.message ?: "加载失败")
        }
    }

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
