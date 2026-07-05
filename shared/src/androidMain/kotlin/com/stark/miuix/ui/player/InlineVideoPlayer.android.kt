package com.stark.miuix.ui.player

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.stark.miuix.ui.icons.IconFullscreen
import com.stark.miuix.ui.icons.IconPause
import com.stark.miuix.ui.icons.IconPlay
import com.stark.miuix.util.VideoPlayerState
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Android 内嵌迷你播放器 — 不强制横屏，显示在详情页顶部
 *
 * 提供：播放/暂停、进度条（可点击跳进度）、全屏按钮
 */
@Composable
actual fun InlineVideoPlayer(
    url: String,
    title: String,
    modifier: Modifier,
    onRequestFullscreen: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }

    // DisposableEffect(exoPlayer) 確保 URL 變化時舊 ExoPlayer 正確釋放
    DisposableEffect(exoPlayer) {
        VideoPlayerState.isPlaying = true
        onDispose {
            VideoPlayerState.isPlaying = false
            exoPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(1L)
            isPlaying = exoPlayer.isPlaying
            VideoPlayerState.isPlaying = exoPlayer.isPlaying
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        // ExoPlayer 视图
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setOnClickListener {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        isPlaying = exoPlayer.isPlaying
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 底部控制层
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 播放/暂停
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        if (isPlaying) IconPause else IconPlay
                    ),
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                    modifier = Modifier.size(24.dp).clickable {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    }
                )

                // 当前时间
                Text(
                    text = formatInlineTime(currentPosition),
                    style = MiuixTheme.textStyles.footnote2,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // 可点击进度条
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .pointerInput(duration) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                exoPlayer.seekTo((fraction * duration).toLong())
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                    val barWidth = maxWidth
                    Box(
                        modifier = Modifier.fillMaxWidth().height(2.dp)
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.dp))
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(progress).height(2.dp)
                            .background(Color.White, RoundedCornerShape(1.dp))
                            .align(Alignment.CenterStart)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = (barWidth * progress).coerceAtMost(barWidth - 4.dp))
                            .size(8.dp)
                            .background(Color.White, RoundedCornerShape(50))
                    )
                }

                // 总时长
                Text(
                    text = formatInlineTime(duration),
                    style = MiuixTheme.textStyles.footnote2,
                    color = Color.White.copy(alpha = 0.6f)
                )

                // 全屏按钮
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

private fun formatInlineTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
