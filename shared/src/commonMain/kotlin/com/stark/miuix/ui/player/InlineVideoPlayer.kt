package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 统一视频播放器（expect 声明）
 *
 * - isExpanded=false → 16:9 小窗 + 基础控制
 * - isExpanded=true  → 全屏放大 + gesture controls 叠加层
 * - 切换时共用同一 ExoPlayer，不重新加载
 */
@Composable
expect fun InlineVideoPlayer(
    url: String,
    title: String,
    modifier: Modifier = Modifier,
    onRequestFullscreen: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onPositionChanged: (Long) -> Unit = {},
    isFullscreen: Boolean = false
)
