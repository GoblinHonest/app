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
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 视频源管理页 UI 状态 */
sealed interface SourceManageUiState {
    data object Idle : SourceManageUiState
    data object Loading : SourceManageUiState
    data class ImportSuccess(val count: Int) : SourceManageUiState
    data class ImportError(val message: String) : SourceManageUiState
}

/**
 * 视频源管理 ViewModel
 *
 * 支持 JSON 文本导入和远程 URL 订阅导入两种方式。
 */
class SourceManageViewModel(
    private val sourceRepository: SourceRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<SourceManageUiState>(SourceManageUiState.Idle)
    val uiState: StateFlow<SourceManageUiState> = _uiState.asStateFlow()

    val sources = sourceRepository.sources

    private val networkClient = NetworkClient()

    /** 从 JSON 字符串导入 */
    fun importSource(jsonString: String) {
        coroutineScope.launch {
            val result = sourceRepository.importFromJson(jsonString)
            result.fold(
                onSuccess = { count -> _uiState.value = SourceManageUiState.ImportSuccess(count) },
                onFailure = { e -> _uiState.value = SourceManageUiState.ImportError(e.message ?: "格式错误") }
            )
        }
    }

    /** 从远程 URL 获取 JSON 并导入 */
    fun importFromUrl(url: String) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _uiState.value = SourceManageUiState.ImportError("请输入有效的 URL (http/https)")
            return
        }
        coroutineScope.launch {
            _uiState.value = SourceManageUiState.Loading
            try {
                val json = networkClient.get(url)
                if (json.isBlank()) {
                    _uiState.value = SourceManageUiState.ImportError("远程地址返回空内容")
                    return@launch
                }
                val result = sourceRepository.importFromJson(json)
                result.fold(
                    onSuccess = { count ->
                        if (count == 0) {
                            _uiState.value = SourceManageUiState.ImportError("未检测到有效的视频源配置")
                        } else {
                            _uiState.value = SourceManageUiState.ImportSuccess(count)
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = SourceManageUiState.ImportError("JSON 解析失败: ${e.message}")
                    }
                )
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> "连接超时，请检查网络"
                    e.message?.contains("resolve", ignoreCase = true) == true -> "域名解析失败，请检查 URL"
                    e.message?.contains("refused", ignoreCase = true) == true -> "连接被拒绝"
                    else -> "网络错误: ${e.message}"
                }
                _uiState.value = SourceManageUiState.ImportError(msg)
            }
        }
    }

    fun toggleSource(sourceName: String) {
        sourceRepository.toggleSource(sourceName)
    }

    fun removeSource(sourceName: String) {
        sourceRepository.removeSource(sourceName)
    }

    fun exportSources(): String {
        return sourceRepository.exportToJson()
    }

    fun clearState() {
        _uiState.value = SourceManageUiState.Idle
    }
}
