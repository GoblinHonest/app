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

package com.stark.miuix.ui.settings

import com.stark.miuix.theme.ThemeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 设置页 UI 状态
 *
 * @property themeMode 主题模式
 * @property dynamicColor 是否启用 Monet 动态取色
 * @property seedColor 种子色（ARGB）
 * @property cacheSize 缓存大小描述
 */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val seedColor: Long = 0xFF3482FF,
    val cacheSize: String = "0 KB"
)

/** 主题模式枚举 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("亮色模式"),
    DARK("暗色模式")
}

/**
 * 设置页 ViewModel
 *
 * 管理应用设置项并同步到 [ThemeState] 全局单例，
 * 实现设置修改实时反映到 UI 主题。
 */
class SettingsViewModel(
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    /** 切换主题模式，同步到全局 [ThemeState] */
    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        ThemeState.setThemeMode(mode)
    }

    /** 切换动态取色，同步到全局 [ThemeState] */
    fun setDynamicColor(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dynamicColor = enabled)
        ThemeState.setDynamicColor(enabled)
    }

    /** 设置种子色，同步到全局 [ThemeState] */
    fun setSeedColor(color: Long) {
        _uiState.value = _uiState.value.copy(seedColor = color)
        ThemeState.setSeedColor(color)
    }

    /** 清除缓存 */
    fun clearCache() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(cacheSize = "0 KB")
        }
    }
}
