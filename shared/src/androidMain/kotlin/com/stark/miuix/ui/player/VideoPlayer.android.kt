/*
 * Copyright 2024 Stark Industries
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stark.miuix.ui.player

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Android 平台播放器实现
 *
 * 使用 Media3 ExoPlayer 实现视频播放：
 * - 自动管理播放器生命周期
 * - 全屏沉浸式播放
 * - 支持 m3u8 / mp4 等常见格式
 *
 * @param url 视频 URL
 * @param title 视频标题
 * @param modifier Modifier
 */
@Composable
actual fun VideoPlayer(
    url: String,
    title: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    // 创建并管理 ExoPlayer 实例
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(url))
                .build()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(url) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNotBlank()) {
            // 使用 Media3 PlayerView 播放视频
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 无播放地址时显示占位提示
            Text(
                text = "无可用播放地址",
                style = MiuixTheme.textStyles.body1,
                color = Color.White
            )
        }
    }
}
