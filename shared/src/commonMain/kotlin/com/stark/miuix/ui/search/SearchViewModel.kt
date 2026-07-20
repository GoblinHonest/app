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

import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.data.source.SuggestionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 搜索页 UI 状态 */
sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Searching : SearchUiState
    data class Success(val keyword: String, val results: List<SearchResult>) : SearchUiState
    data class Error(val keyword: String, val message: String) : SearchUiState
}

/**
 * 搜索页 ViewModel
 *
 * 内置 500ms 输入防抖：用户停止输入后自动触发搜索，
 * 减少无效网络请求，提升响应流畅度。
 */
class SearchViewModel(
    private val videoRepository: VideoRepository,
    private val suggestionService: SuggestionService? = null,
    private val sourceRepository: SourceRepository? = null
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _hotSearches = MutableStateFlow(DEFAULT_HOT_SEARCHES)
    val hotSearches: StateFlow<List<String>> = _hotSearches.asStateFlow()

    val searchHistory = videoRepository.searchHistory

    private var debounceJob: Job? = null
    private var suggestJob: Job? = null

    init {
        refreshHotSearches()
    }

    /**
     * 防抖搜索 — 每次输入变更调用，500ms 无新输入后自动执行
     */
    fun onQueryChanged(keyword: String) {
        debounceJob?.cancel()
        suggestJob?.cancel()
        if (keyword.isBlank()) {
            _uiState.value = SearchUiState.Idle
            _suggestions.value = emptyList()
            return
        }
        // 300ms 后获取联想词
        suggestJob = coroutineScope.launch {
            delay(SUGGEST_DEBOUNCE_MS)
            fetchSuggestions(keyword)
        }
        // 500ms 后执行搜索
        debounceJob = coroutineScope.launch {
            delay(DEBOUNCE_MS)
            _suggestions.value = emptyList()
            executeSearch(keyword)
        }
    }

    private suspend fun fetchSuggestions(keyword: String) {
        val service = suggestionService ?: return
        val repo = sourceRepository ?: return
        val sources = repo.getEnabledSources()
        if (sources.isEmpty()) return
        val result = service.getSuggestions(sources, keyword)
        _suggestions.value = result
    }

    /** 刷新热搜榜：优先用搜索历史热词，不足时补默认热搜 */
    fun refreshHotSearches() {
        coroutineScope.launch {
            val history = videoRepository.searchHistory.value
            val merged = (history + DEFAULT_HOT_SEARCHES)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(HOT_SEARCH_LIMIT)
            _hotSearches.value = merged.ifEmpty { DEFAULT_HOT_SEARCHES }
        }
    }

    /** 立即执行搜索（点击按钮或选择历史记录时） */
    fun search(keyword: String) {
        if (keyword.isBlank()) return
        debounceJob?.cancel()
        suggestJob?.cancel()
        _suggestions.value = emptyList()
        coroutineScope.launch { executeSearch(keyword) }
    }

    fun clearHistory() {
        videoRepository.clearHistory()
        refreshHotSearches()
    }

    fun removeHistoryItem(keyword: String) {
        videoRepository.removeHistoryItem(keyword)
        refreshHotSearches()
    }

    fun resetToIdle() {
        debounceJob?.cancel()
        suggestJob?.cancel()
        _suggestions.value = emptyList()
        _uiState.value = SearchUiState.Idle
    }

    private suspend fun executeSearch(keyword: String) {
        _uiState.value = SearchUiState.Searching
        val result = videoRepository.search(keyword)
        result.fold(
            onSuccess = { videos ->
                _uiState.value = SearchUiState.Success(keyword, videos)
                refreshHotSearches()
            },
            onFailure = { error ->
                _uiState.value = SearchUiState.Error(keyword, error.message ?: "搜索失败")
            }
        )
    }

    companion object {
        private const val DEBOUNCE_MS = 500L
        private const val SUGGEST_DEBOUNCE_MS = 300L
        private const val HOT_SEARCH_LIMIT = 10
        private val DEFAULT_HOT_SEARCHES = listOf(
            "庆余年", "长相思", "繁花", "三体", "狂飙",
            "奥本海默", "流浪地球", "权力的游戏", "海贼王", "进击的巨人"
        )
    }
}
