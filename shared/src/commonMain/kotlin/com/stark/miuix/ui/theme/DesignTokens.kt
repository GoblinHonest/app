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

package com.stark.miuix.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 设计规范 Token
 *
 * 参考主流视频 App（B站/Netflix/YouTube）的间距、圆角、尺寸体系。
 * 全局统一使用，确保视觉一致性。
 */
object DesignTokens {
    /** 页面水平内边距 */
    val screenPadding = 16.dp

    /** 卡片间距 */
    val cardGap = 10.dp

    /** 小间距（标签之间、文字行间） */
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp

    /** 圆角 */
    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusLg = 16.dp
    val radiusXl = 20.dp

    /** 卡片封面比例 */
    const val coverAspectRatio = 16f / 10f

    /** 网格最小列宽（手机 2 列，平板 3-4 列） */
    val gridMinWidth = 160.dp

    /** 底部导航栏高度 */
    val bottomBarHeight = 56.dp

    /** 搜索栏高度 */
    val searchBarHeight = 44.dp
}
