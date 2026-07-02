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

package com.stark.miuix.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.stark.miuix.ui.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 全局主题状态
 *
 * 单例对象，持有主题模式和动态取色开关，
 * 供 [AppTheme] 和 [SettingsViewModel] 双向绑定。
 */
object ThemeState {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(true)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _seedColor = MutableStateFlow(0xFF3482FF)
    val seedColor: StateFlow<Long> = _seedColor.asStateFlow()

    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }
    fun setDynamicColor(enabled: Boolean) { _dynamicColor.value = enabled }
    fun setSeedColor(color: Long) { _seedColor.value = color }
}

/**
 * 应用主题配置
 *
 * 基于 Miuix 的 HyperOS 风格主题系统，支持：
 * - 亮色/暗色/跟随系统三种模式
 * - Monet 动态取色（基于种子色生成完整调色板）
 *
 * 主题模式由 [ThemeState] 驱动，设置页修改即时生效。
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val themeMode by ThemeState.themeMode.collectAsState()
    val dynamicColor by ThemeState.dynamicColor.collectAsState()
    val seedColor by ThemeState.seedColor.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    val colorSchemeMode = remember(themeMode, dynamicColor) {
        if (dynamicColor) {
            ColorSchemeMode.MonetSystem
        } else {
            when (themeMode) {
                ThemeMode.SYSTEM -> ColorSchemeMode.MonetSystem
                ThemeMode.LIGHT -> ColorSchemeMode.Light
                ThemeMode.DARK -> ColorSchemeMode.Dark
            }
        }
    }

    val controller = remember(colorSchemeMode, seedColor) {
        ThemeController(
            colorSchemeMode = colorSchemeMode,
            keyColor = Color(seedColor)
        )
    }

    MiuixTheme(
        controller = controller,
        content = content
    )
}
