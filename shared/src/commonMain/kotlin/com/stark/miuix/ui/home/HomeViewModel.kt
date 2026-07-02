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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 首页 UI 状态
 *
 * 使用密封接口定义首页的所有可能状态，确保状态管理的类型安全。
 */
sealed interface HomeUiState {
    /** 加载中状态 */
    data object Loading : HomeUiState

    /** 成功加载状态
     * @property videos 推荐视频列表
     * @property sources 已启用的视频源列表
     */
    data class Success(
        val videos: List<SearchResult>,
        val sources: List<VideoSource>
    ) : HomeUiState

    /** 错误状态
     * @property message 错误信息
     */
    data class Error(val message: String) : HomeUiState
}

/**
 * 首页 ViewModel
 *
 * 管理首页的业务逻辑和 UI 状态，包括：
 * - 加载推荐视频
 * - 管理视频源列表
 * - 处理用户交互
 *
 * @property videoRepository 视频仓库
 * @property sourceRepository 视频源仓库
 * @property coroutineScope 协程作用域
 */
class HomeViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /** 首页 UI 状态 */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * 加载首页数据
     *
     * 从所有已启用的视频源获取分类视频，合并后展示。
     * 如果没有启用的视频源，显示空状态。
     */
    fun loadVideos() {
        coroutineScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val sources = sourceRepository.getEnabledSources()
                if (sources.isEmpty()) {
                    _uiState.value = HomeUiState.Success(
                        videos = emptyList(),
                        sources = emptyList()
                    )
                    return@launch
                }

                // 从第一个启用的源获取分类视频
                val result = videoRepository.getCategoryVideos(sources.first())
                result.fold(
                    onSuccess = { videos ->
                        _uiState.value = HomeUiState.Success(
                            videos = videos,
                            sources = sources
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = HomeUiState.Error(
                            error.message ?: "加载失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "未知错误"
                )
            }
        }
    }
}
