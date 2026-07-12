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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

/** 首页 UI 状态 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val videos: List<SearchResult>,
        val currentCategory: String
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/** 首页分类 Tab 定义 */
val HOME_TABS = listOf("推荐", "电视剧", "动漫", "电影")

/**
 * 首页 ViewModel — 多源聚合 + 分类切换
 *
 * - "推荐" Tab：每源默认分类混排去重
 * - 其他 Tab：按 categoryUrls 映射加载对应分类
 * 使用 [supervisorScope] 并行加载，单个源失败不影响其他源。
 */
class HomeViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentCategory = MutableStateFlow("推荐")
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    private var loaded = false

    fun loadVideos() {
        if (loaded) return
        loaded = true
        AppLogger.d("Home", "loadVideos() 首次调用, category=${_currentCategory.value}")
        scope.launch {
            fetchData(_currentCategory.value)
        }
    }

    /** 切换分类 Tab */
    fun switchCategory(category: String) {
        if (_currentCategory.value == category) return
        _currentCategory.value = category
        _uiState.value = HomeUiState.Loading
        AppLogger.d("Home", "switchCategory: $category")
        scope.launch {
            fetchData(category)
        }
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            fetchData(_currentCategory.value)
            delay(800)
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchData(category: String) {
        try {
            val sources = sourceRepository.getEnabledSources()
            AppLogger.d("Home", "加载分类[$category], 启用源数: ${sources.size}")

            if (sources.isEmpty()) {
                _uiState.value = HomeUiState.Success(videos = emptyList(), currentCategory = category)
                return
            }

            val isRecommend = category == "推荐"

            val allVideos = coroutineScope {
                sources.map { source ->
                    async(Dispatchers.Default) {
                        try {
                            val categoryUrl = if (isRecommend) {
                                ""
                            } else {
                                source.categoryRule.categoryUrls[category] ?: ""
                            }
                            if (!isRecommend && categoryUrl.isBlank()) {
                                AppLogger.d("Home", "源[${source.sourceName}] 无[$category]分类, 跳过")
                                return@async emptyList()
                            }
                            val result = videoRepository.getCategoryVideos(source, categoryUrl)
                            val videos = result.getOrDefault(emptyList()).take(PER_SOURCE_LIMIT)
                            AppLogger.d("Home", "源[${source.sourceName}][$category] 加载 ${videos.size} 条")
                            videos
                        } catch (e: Exception) {
                            AppLogger.e("Home", "源[${source.sourceName}][$category] 加载失败: ${e.message}")
                            emptyList()
                        }
                    }
                }.awaitAll()
            }

            val merged = interleave(allVideos).deduplicateByTitle()
            AppLogger.d("Home", "[$category] 聚合完成, 去重后 ${merged.size} 条")
            _uiState.value = HomeUiState.Success(videos = merged, currentCategory = category)
        } catch (e: Exception) {
            AppLogger.e("Home", "[$category] 加载异常", e)
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

    private fun List<SearchResult>.deduplicateByTitle(): List<SearchResult> {
        val seen = mutableSetOf<String>()
        return filter { seen.add(it.title.trim().lowercase()) }
    }

    companion object {
        private const val PER_SOURCE_LIMIT = 12
    }
}
