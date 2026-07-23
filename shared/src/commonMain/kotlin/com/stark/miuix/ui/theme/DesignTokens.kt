/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 全局设计 Token — CineHub 设计规范 v2
 *
 * 基于 HyperOS 设计语言，暗色优先，内容沉浸式体验。
 */
object DesignTokens {

    // ─── 间距系统 (4dp 基础单位) ───
    val spacing2xs = 2.dp
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp
    val spacing2xl = 32.dp
    val spacing3xl = 48.dp

    val screenPadding = 16.dp

    // ─── 圆角系统 ───
    val radiusXs = 4.dp
    val radiusSm = 6.dp
    val radiusMd = 10.dp
    val radiusCard = 12.dp
    val radiusLg = 14.dp
    val radiusXl = 20.dp
    val radius2xl = 24.dp
    val radiusPill = 100.dp

    // ─── 尺寸 ───
    val searchBarHeight = 44.dp
    val bottomBarHeight = 56.dp
    val touchTargetMin = 44.dp
    val iconSizeSm = 18.dp
    val iconSizeMd = 22.dp
    val iconSizeLg = 24.dp
    val iconSizeXl = 28.dp
    const val coverAspectRatio = 2f / 3f
    const val bannerAspectRatio = 16f / 9f
    const val continueWatchAspectRatio = 16f / 10f

    // ─── 品牌色原始值（UI 请用 AppColors.brand() 以适配亮/暗） ───
    val brandBlue = Color(0xFF2E6BE6)
    val brandBlueLight = Color(0xFF5B9BF5)
    val brandBlueDark = Color(0xFF1D4FB8)
    val brandPurple = Color(0xFF6C63FF)

    // ─── 品牌色 (Dark 模式提亮，经 AppColors.brand() 自动选用) ───
    val brandBlueDarkMode = Color(0xFF5B9BF5)

    // ─── 功能色 ───
    val success = Color(0xFF34D399)
    val warning = Color(0xFFFBBF24)
    val error = Color(0xFFF87171)
    val info = Color(0xFF60A5FA)
    val gold = Color(0xFFF59E0B)

    // ─── 状态 badge 色 ───
    val badgeRed = Color(0xFFE11D48)
    val badgeGreen = Color(0xFF22C55E)
    val badgeOrange = Color(0xFFFF6B35)

    // ─── 播放器 ───
    val videoBackground = Color(0xFF0F0F0F)
    val playerOverlay = Color(0x99000000)

    // ─── 渐变遮罩 ───
    val posterGradientStart = Color(0x00000000)
    val posterGradientEnd = Color(0xD9000000)
    val bannerGradientEnd = Color(0xB3000000)

    // ─── 动效时长 ───
    const val animFast = 150
    const val animNormal = 200
    const val animMedium = 250
    const val animSlow = 300
    const val animPage = 300
    const val shimmerDuration = 1200

    // ─── 响应式断点 ───
    val breakpointCompact = 600.dp
    val breakpointMedium = 840.dp
    val breakpointExpanded = 1200.dp
}
