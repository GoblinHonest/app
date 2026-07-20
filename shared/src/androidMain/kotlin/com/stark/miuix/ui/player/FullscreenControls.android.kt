package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable
import com.stark.miuix.data.dlna.DlnaController

/**
 * Android 全屏播放器叠加控件 — 完整手势支持 + 投屏
 */
@Composable
actual fun FullscreenControls(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit,
    isBuffering: Boolean,
    dlnaController: DlnaController?
) {
    FullscreenPlayerOverlay(
        url = url,
        title = title,
        onExitFullscreen = onExitFullscreen,
        isBuffering = isBuffering,
        dlnaController = dlnaController
    )
}
