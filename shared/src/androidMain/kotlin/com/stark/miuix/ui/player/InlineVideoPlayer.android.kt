/*
 * Copyright 2024 Stark Industries
 *
 * Android 内嵌迷你播放器 — 共享 PlayerStore 中的 ExoPlayer
 * 全屏切换时不释放播放器，由 FullscreenPlayerOverlay 叠加控件
 */
package com.stark.miuix.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.stark.miuix.ui.icons.IconFullscreen
import com.stark.miuix.ui.icons.IconPause
import com.stark.miuix.ui.icons.IconPlay
import com.stark.miuix.util.VideoPlayerState
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
actual fun InlineVideoPlayer(
    url: String,
    title: String,
    modifier: Modifier,
    onRequestFullscreen: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onPositionChanged: (Long) -> Unit,
    isFullscreen: Boolean,
    onBufferingChanged: ((Boolean) -> Unit)?
) {
    val context = LocalContext.current

    val exoPlayer = remember(url) {
        PlayerStore.getOrCreate(context, url).also {
            PlayerStore.currentUrl = url
            PlayerStore.savedTitle = title
        }
    }

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(exoPlayer.currentPosition) }
    var duration by remember { mutableLongStateOf(exoPlayer.duration.coerceAtLeast(1L)) }
    var isBuffering by remember { mutableStateOf(exoPlayer.playbackState == Player.STATE_BUFFERING) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                onBufferingChanged?.invoke(state == Player.STATE_BUFFERING)
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                PlayerStore.isPlaying = playing
                VideoPlayerState.isPlaying = playing
            }
            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                onBufferingChanged?.invoke(false)
            }
        }
        exoPlayer.addListener(listener)
        VideoPlayerState.isPlaying = false
        onDispose {
            exoPlayer.removeListener(listener)
            PlayerStore.savedPosition = exoPlayer.currentPosition
            PlayerStore.isPlaying = false
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (!isLoading && errorMessage == null) {
                currentPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration.coerceAtLeast(1L)
                onPositionChanged(currentPosition)
            }
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(16f / 9f))
            .background(Color.Black)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正在解析播放地址...", style = MiuixTheme.textStyles.footnote2, color = Color.White.copy(alpha = 0.7f))
                }
            }
            return@Box
        }

        if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage, style = MiuixTheme.textStyles.footnote2, color = Color.White.copy(alpha = 0.8f))
            }
            return@Box
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            update = { view -> if (view.player != exoPlayer) view.player = exoPlayer },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            }
        }

        if (!isFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(if (isPlaying) IconPause else IconPlay),
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp).clickable {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }
                    )
                    Text(fmt(currentPosition), style = MiuixTheme.textStyles.footnote2, color = Color.White.copy(alpha = 0.9f))
                    BoxWithConstraints(
                        modifier = Modifier.weight(1f).height(16.dp).pointerInput(duration) {
                            detectTapGestures { offset ->
                                val wasPlaying = exoPlayer.isPlaying
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                exoPlayer.seekTo((fraction * duration).toLong())
                                if (wasPlaying) exoPlayer.play()
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                        val barWidth = maxWidth
                        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.dp)))
                        Box(modifier = Modifier.fillMaxWidth(progress).height(2.dp).background(Color.White, RoundedCornerShape(1.dp)).align(Alignment.CenterStart))
                        Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = (barWidth * progress).coerceAtMost(barWidth - 4.dp)).size(8.dp).background(Color.White, RoundedCornerShape(50)))
                    }
                    Text(fmt(duration), style = MiuixTheme.textStyles.footnote2, color = Color.White.copy(alpha = 0.6f))
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconFullscreen),
                        contentDescription = "全屏",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp).clickable(onClick = onRequestFullscreen)
                    )
                }
            }
        }
    }
}

private fun fmt(ms: Long): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
