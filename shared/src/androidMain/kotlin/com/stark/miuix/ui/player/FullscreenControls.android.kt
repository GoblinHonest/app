package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable

/**
 * Android 全屏播放器叠加控件 — 完整手势支持
 */
@Composable
actual fun FullscreenControls(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit
) {
    FullscreenPlayerOverlay(
        url = url,
        title = title,
        onExitFullscreen = onExitFullscreen
    )
}
