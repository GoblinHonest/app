package com.stark.miuix.ui.player

/**
 * Android: 释放 ExoPlayer
 */
actual fun releaseSharedPlayer() {
    PlayerStore.release()
}
