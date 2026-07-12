package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable

@Composable
actual fun FullscreenControls(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit,
    isBuffering: Boolean
) {
    // WasmJs: no-op
}
