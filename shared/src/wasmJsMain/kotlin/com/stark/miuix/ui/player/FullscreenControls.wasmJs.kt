package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable
import com.stark.miuix.data.dlna.DlnaController

@Composable
actual fun FullscreenControls(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit,
    isBuffering: Boolean,
    dlnaController: DlnaController?
) {
    // WasmJs: no-op
}
