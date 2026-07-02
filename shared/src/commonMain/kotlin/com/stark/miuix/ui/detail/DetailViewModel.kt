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

package com.stark.miuix.ui.detail

import com.stark.miuix.data.model.Video
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 详情页 UI 状态
 */
sealed interface DetailUiState {
    /** 加载中 */
    data object Loading : DetailUiState

    /** 加载成功
     * @property video 视频详情数据
     */
    data class Success(val video: Video) : DetailUiState

    /** 加载失败
     * @property message 错误信息
     */
    data class Error(val message: String) : DetailUiState
}

/**
 * 详情页 ViewModel
 *
 * 负责加载视频详情、解析播放地址。
 *
 * @property videoRepository 视频仓库
 * @property sourceRepository 视频源仓库
 * @property coroutineScope 协程作用域
 */
class DetailViewModel(
    private val videoRepository: VideoRepository,
    private val sourceRepository: SourceRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)

    /** 详情页 UI 状态 */
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * 加载视频详情
     *
     * @param sourceName 视频源名称
     * @param detailUrl 详情页 URL
     */
    fun loadDetail(sourceName: String, detailUrl: String) {
        coroutineScope.launch {
            _uiState.value = DetailUiState.Loading
            val source = sourceRepository.getSourceByName(sourceName)
            if (source == null) {
                _uiState.value = DetailUiState.Error("视频源不存在")
                return@launch
            }
            val result = videoRepository.getDetail(source, detailUrl)
            result.fold(
                onSuccess = { video ->
                    _uiState.value = DetailUiState.Success(video)
                },
                onFailure = { error ->
                    _uiState.value = DetailUiState.Error(error.message ?: "加载失败")
                }
            )
        }
    }

    /**
     * 获取播放地址
     *
     * @param sourceName 视频源名称
     * @param episodeUrl 剧集 URL
     * @return 可播放 URL
     */
    suspend fun getPlayerUrl(sourceName: String, episodeUrl: String): Result<String> {
        val source = sourceRepository.getSourceByName(sourceName)
            ?: return Result.failure(Exception("视频源不存在"))
        return videoRepository.getPlayerUrl(source, episodeUrl)
    }
}
