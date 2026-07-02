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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 搜索页 UI 状态
 */
sealed interface SearchUiState {
    /** 空闲状态，等待用户输入 */
    data object Idle : SearchUiState

    /** 搜索中 */
    data object Searching : SearchUiState

    /** 搜索成功
     * @property keyword 搜索关键词
     * @property results 搜索结果列表
     */
    data class Success(
        val keyword: String,
        val results: List<SearchResult>
    ) : SearchUiState

    /** 搜索失败
     * @property keyword 搜索关键词
     * @property message 错误信息
     */
    data class Error(
        val keyword: String,
        val message: String
    ) : SearchUiState
}

/**
 * 搜索页 ViewModel
 *
 * @property videoRepository 视频仓库
 * @property coroutineScope 协程作用域
 */
class SearchViewModel(
    private val videoRepository: VideoRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)

    /** 搜索 UI 状态 */
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /** 搜索历史 */
    val searchHistory = videoRepository.searchHistory

    /**
     * 执行搜索
     *
     * @param keyword 搜索关键词
     */
    fun search(keyword: String) {
        if (keyword.isBlank()) return

        coroutineScope.launch {
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
    }

    /**
     * 清除搜索历史
     */
    fun clearHistory() {
        videoRepository.clearHistory()
    }
}
