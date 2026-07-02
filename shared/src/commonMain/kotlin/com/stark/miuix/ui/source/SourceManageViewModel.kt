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

package com.stark.miuix.ui.source

import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.repository.SourceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 视频源管理页 UI 状态
 */
sealed interface SourceManageUiState {
    /** 空闲 */
    data object Idle : SourceManageUiState

    /** 导入成功
     * @property count 导入的源数量
     */
    data class ImportSuccess(val count: Int) : SourceManageUiState

    /** 导入失败
     * @property message 错误信息
     */
    data class ImportError(val message: String) : SourceManageUiState
}

/**
 * 视频源管理 ViewModel
 *
 * @property sourceRepository 视频源仓库
 * @property coroutineScope 协程作用域
 */
class SourceManageViewModel(
    private val sourceRepository: SourceRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<SourceManageUiState>(SourceManageUiState.Idle)

    /** UI 状态 */
    val uiState: StateFlow<SourceManageUiState> = _uiState.asStateFlow()

    /** 所有视频源列表 */
    val sources = sourceRepository.sources

    /**
     * 从 JSON 导入视频源
     *
     * @param jsonString JSON 格式的视频源配置
     */
    fun importSource(jsonString: String) {
        coroutineScope.launch {
            val result = sourceRepository.importFromJson(jsonString)
            result.fold(
                onSuccess = { count ->
                    _uiState.value = SourceManageUiState.ImportSuccess(count)
                },
                onFailure = { error ->
                    _uiState.value = SourceManageUiState.ImportError(error.message ?: "导入失败")
                }
            )
        }
    }

    /**
     * 切换视频源启用状态
     *
     * @param sourceName 源名称
     */
    fun toggleSource(sourceName: String) {
        sourceRepository.toggleSource(sourceName)
    }

    /**
     * 删除视频源
     *
     * @param sourceName 源名称
     */
    fun removeSource(sourceName: String) {
        sourceRepository.removeSource(sourceName)
    }

    /**
     * 导出所有视频源为 JSON
     *
     * @return JSON 字符串
     */
    fun exportSources(): String {
        return sourceRepository.exportToJson()
    }

    /**
     * 清除导入状态
     */
    fun clearState() {
        _uiState.value = SourceManageUiState.Idle
    }
}
