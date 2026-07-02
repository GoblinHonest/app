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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * 应用主题配置
 *
 * 基于 Miuix 的 HyperOS 风格主题系统，支持：
 * - 亮色/暗色主题自动切换（跟随系统）
 * - Monet 动态取色（基于种子色生成完整调色板）
 * - Squircle 超椭圆圆角形状
 *
 * @param content 子组件内容
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val controller = remember {
        ThemeController(
            colorSchemeMode = ColorSchemeMode.MonetSystem,
            keyColor = Color(0xFF3482FF) // 蓝色种子色，生成 HyperOS 风格配色
        )
    }

    MiuixTheme(
        controller = controller,
        content = content
    )
}
