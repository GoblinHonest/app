/*
 * Copyright 2024 Stark Industries
 *
 * 全局共享播放器存储池 — 内嵌播放器与全屏播放器共用同一个 ExoPlayer
 */
package com.stark.miuix.ui.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

/**
 * 共享播放器存储池 — 单例对象，生命周期贯穿整个 App
 *
 * 核心职责：
 * 1. 持有唯一 ExoPlayer 实例，避免重复创建
 * 2. 记录当前播放 URL 和播放位置
 * 3. 内嵌播放器与全屏播放器通过此对象无缝交接
 */
object PlayerStore {
    @Volatile
    var exoPlayer: ExoPlayer? = null

    @Volatile
    var currentUrl: String = ""

    @Volatile
    var savedPosition: Long = 0L

    @Volatile
    var savedTitle: String = ""

    @Volatile
    var isPlaying: Boolean = false

    /**
     * 获取或创建 ExoPlayer — 核心方法
     * - URL 匹配且播放器存在 → 直接复用
     * - URL 变化或播放器不存在 → 释放旧播放器，创建新播放器
     */
    fun getOrCreate(context: android.content.Context, url: String): ExoPlayer {
        val existing = exoPlayer
        if (existing != null && currentUrl == url) {
            return existing
        }
        // URL 变化：释放旧播放器
        existing?.release()

        val newPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
        exoPlayer = newPlayer
        currentUrl = url
        savedPosition = 0L
        return newPlayer
    }

    /** 释放播放器（退出详情页时调用） */
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        currentUrl = ""
        savedPosition = 0L
        isPlaying = false
    }
}
