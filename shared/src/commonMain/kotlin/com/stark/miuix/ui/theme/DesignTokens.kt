/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 全局设计 Token — 参考 Bangumi/优酷/B站 App 设计规范
 */
object DesignTokens {
    val screenPadding = 16.dp
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp
    val spacing2xl = 32.dp
    val radiusSm = 6.dp
    val radiusMd = 10.dp
    val radiusLg = 14.dp
    val radiusXl = 20.dp
    val radiusPill = 100.dp
    const val coverAspectRatio = 2f / 3f
    val searchBarHeight = 44.dp  // iOS HIG 最小触控目标
    val bottomBarHeight = 56.dp
    val gridMinWidth = 108.dp
    val cardGap = 8.dp
    val brandBlue = Color(0xFF3478F6)
    val brandBlueDark = Color(0xFF1A5FD4)
    val badgeRed = Color(0xFFE11D48)
    val badgeOrange = Color(0xFFFF6B35)
    val bottomBarAlpha = 0.92f
}
