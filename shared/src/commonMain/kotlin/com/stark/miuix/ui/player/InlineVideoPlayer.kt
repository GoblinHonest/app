package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 内嵌迷你播放器（expect 声明）
 *
 * 在详情页顶部以 16:9 小窗形式播放视频，
 * 提供基础播放控制 + 全屏按钮。
 * 不强制横屏，不隐藏系统栏。
 *
 * @param url 视频地址
 * @param title 视频标题
 * @param modifier Modifier
 * @param onRequestFullscreen 点击全屏时回调，由外层导航到全屏 PlayerScreen
 */
@Composable
expect fun InlineVideoPlayer(
    url: String,
    title: String,
    modifier: Modifier = Modifier,
    onRequestFullscreen: () -> Unit
)
