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
import com.stark.miuix.data.repository.VideoRepository
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
    private val videoRepository: VideoRepository
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val searchHistory = videoRepository.searchHistory

    private var debounceJob: Job? = null

    /**
     * 防抖搜索 — 每次输入变更调用，500ms 无新输入后自动执行
     */
    fun onQueryChanged(keyword: String) {
        debounceJob?.cancel()
        if (keyword.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        debounceJob = coroutineScope.launch {
            delay(DEBOUNCE_MS)
            executeSearch(keyword)
        }
    }

    /** 立即执行搜索（点击按钮或选择历史记录时） */
    fun search(keyword: String) {
        if (keyword.isBlank()) return
        debounceJob?.cancel()
        coroutineScope.launch { executeSearch(keyword) }
    }

    fun clearHistory() {
        videoRepository.clearHistory()
    }

    fun removeHistoryItem(keyword: String) {
        videoRepository.removeHistoryItem(keyword)
    }

    private suspend fun executeSearch(keyword: String) {
        _uiState.value = SearchUiState.Searching
        val result = videoRepository.search(keyword)
        result.fold(
            onSuccess = { videos ->
                _uiState.value = SearchUiState.Success(keyword, videos)
            },
            onFailure = { error ->
                _uiState.value = SearchUiState.Error(keyword, error.message ?: "搜索失败")
            }
        )
    }

    companion object {
        private const val DEBOUNCE_MS = 500L
    }
}
