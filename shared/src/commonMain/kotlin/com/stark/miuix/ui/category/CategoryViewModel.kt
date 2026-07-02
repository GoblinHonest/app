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

/**
 * 分类页 UI 状态
 */
sealed interface CategoryUiState {
    /** 加载中 */
    data object Loading : CategoryUiState

    /** 加载成功
     * @property videos 分类下的视频列表
     * @property currentCategory 当前分类名称
     */
    data class Success(
        val videos: List<SearchResult>,
        val currentCategory: String = ""
    ) : CategoryUiState

    /** 加载失败
     * @property message 错误信息
     */
    data class Error(val message: String) : CategoryUiState
}

/**
 * 分类页 ViewModel
 *
 * @property videoRepository 视频仓库
 * @property sourceRepository 视频源仓库
 * @property coroutineScope 协程作用域
 */
class CategoryViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)

    /** 分类页 UI 状态 */
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    /** 当前页码 */
    private var currentPage = 1

    /**
     * 加载分类视频
     *
     * @param sourceName 视频源名称
     * @param categoryUrl 分类 URL
     * @param page 页码
     */
    fun loadCategoryVideos(sourceName: String, categoryUrl: String, page: Int = 1) {
        coroutineScope.launch {
            _uiState.value = CategoryUiState.Loading
            val source = sourceRepository.getSourceByName(sourceName)
            if (source == null) {
                _uiState.value = CategoryUiState.Error("视频源不存在")
                return@launch
            }
            currentPage = page
            val result = videoRepository.getCategoryVideos(source, categoryUrl, page)
            result.fold(
                onSuccess = { videos ->
                    _uiState.value = CategoryUiState.Success(videos)
                },
                onFailure = { error ->
                    _uiState.value = CategoryUiState.Error(error.message ?: "加载失败")
                }
            )
        }
    }

    /**
     * 加载下一页
     */
    fun loadNextPage(sourceName: String, categoryUrl: String) {
        loadCategoryVideos(sourceName, categoryUrl, currentPage + 1)
    }
}
