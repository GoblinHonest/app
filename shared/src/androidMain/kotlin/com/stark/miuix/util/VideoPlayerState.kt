package com.stark.miuix.util

/** 全局视频播放状态 — VideoPlayer 更新，MainActivity 读取判断是否触发 PiP */
object VideoPlayerState {
    @Volatile
    var isPlaying: Boolean = false
}
