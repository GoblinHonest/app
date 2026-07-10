package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable

@Composable
actual fun FullscreenControls(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit
) {
    // Desktop: no-op (inline controls handle everything)
}
