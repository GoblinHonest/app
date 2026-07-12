package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable

/**
 * 全屏播放器叠加控件（expect 声明）
 *
 * 各平台根据自身能力提供全屏手势控制 UI：
 * - Android: 完整手势（亮度/音量/进度/倍速/锁屏）
 * - 其他: 留空（使用内嵌控制条替代）
 */
@Composable
expect fun FullscreenControls(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit,
    isBuffering: Boolean = false
)
