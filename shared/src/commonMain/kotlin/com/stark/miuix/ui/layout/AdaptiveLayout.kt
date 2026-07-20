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

package com.stark.miuix.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 屏幕尺寸断点
 */
enum class ScreenSize {
    COMPACT,
    MEDIUM,
    EXPANDED,
    LARGE
}

/**
 * 根据当前可用宽度判断屏幕尺寸类别
 *
 * - COMPACT: < 600dp（手机竖屏）
 * - MEDIUM: 600dp ~ 839dp（手机横屏 / 小平板）
 * - EXPANDED: 840dp ~ 1199dp（平板）
 * - LARGE: >= 1200dp（TV / 大屏）
 */
@Composable
fun adaptiveLayout(
    compact: @Composable BoxWithConstraintsScope.() -> Unit,
    medium: @Composable BoxWithConstraintsScope.() -> Unit = compact,
    expanded: @Composable BoxWithConstraintsScope.() -> Unit = medium,
    large: @Composable BoxWithConstraintsScope.() -> Unit = expanded
) {
    BoxWithConstraints {
        when {
            maxWidth < 600.dp -> compact()
            maxWidth < 840.dp -> medium()
            maxWidth < 1200.dp -> expanded()
            else -> large()
        }
    }
}

/**
 * 获取当前屏幕尺寸类别
 */
@Composable
fun currentScreenSize(): ScreenSize {
    var result = ScreenSize.COMPACT
    BoxWithConstraints {
        result = when {
            maxWidth < 600.dp -> ScreenSize.COMPACT
            maxWidth < 840.dp -> ScreenSize.MEDIUM
            maxWidth < 1200.dp -> ScreenSize.EXPANDED
            else -> ScreenSize.LARGE
        }
    }
    return result
}

/**
 * 响应式网格列数
 */
@Composable
fun adaptiveGridColumns(): Int {
    var columns = 3
    BoxWithConstraints {
        columns = when {
            maxWidth < 600.dp -> 3
            maxWidth < 840.dp -> 4
            maxWidth < 1200.dp -> 5
            else -> 6
        }
    }
    return columns
}

/**
 * 响应式屏幕内边距
 */
@Composable
fun adaptiveScreenPadding(): Dp {
    var padding = 16.dp
    BoxWithConstraints {
        padding = when {
            maxWidth < 600.dp -> 16.dp
            maxWidth < 840.dp -> 24.dp
            maxWidth < 1200.dp -> 32.dp
            else -> 48.dp
        }
    }
    return padding
}
