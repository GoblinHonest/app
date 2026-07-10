package com.stark.miuix.util

/** 全局视频播放状态 — VideoPlayer 更新，MainActivity 读取判断是否触发 PiP */
object VideoPlayerState {
    @Volatile
    var isPlaying: Boolean = false

    /** 播放位置（毫秒），用于内嵌播放器→全屏播放器的无缝衔接 */
    @Volatile
    var savedPosition: Long = 0L

    /** 当前播放 URL，用于判断是否需要恢复位置 */
    @Volatile
    var currentUrl: String = ""
}
