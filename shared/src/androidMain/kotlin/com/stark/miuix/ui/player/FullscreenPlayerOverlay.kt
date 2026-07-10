/*
 * Copyright 2024 Stark Industries
 *
 * 全屏播放器叠加控件 — 透明覆盖层，不渲染视频，只处理手势和绘制控件
 * 视频由 InlineVideoPlayer 渲染，此组件叠加在上面
 */
package com.stark.miuix.ui.player

import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconExitFullscreen
import com.stark.miuix.ui.icons.IconFloating
import com.stark.miuix.ui.icons.IconHD
import com.stark.miuix.ui.icons.IconLock
import com.stark.miuix.ui.icons.IconNext
import com.stark.miuix.ui.icons.IconPause
import com.stark.miuix.ui.icons.IconPlay
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@UnstableApi
@Composable
fun FullscreenPlayerOverlay(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val exoPlayer = remember(url) {
        PlayerStore.exoPlayer ?: PlayerStore.getOrCreate(context, url)
    }

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }
    var showControls by remember { mutableStateOf(true) }
    var gestureText by remember { mutableStateOf("") }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var isLocked by remember { mutableStateOf(false) }

    // 全屏 + 隐藏系统栏
    DisposableEffect(Unit) {
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        @Suppress("DEPRECATION")
        activity?.window?.decorView?.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
        onDispose {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            @Suppress("DEPRECATION")
            activity?.window?.decorView?.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    // 轮询进度
    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(1L)
            delay(500)
        }
    }

    // 自动隐藏控制层
    LaunchedEffect(showControls) {
        if (showControls && !isLocked) {
            delay(4000)
            showControls = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ====== 手势捕获区（透明，覆盖整个屏幕）======
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isLocked) {
                    detectTapGestures(
                        onTap = {
                            if (isLocked) isLocked = false
                            else showControls = !showControls
                        },
                        onLongPress = {
                            // 长按 2x 加速
                            exoPlayer.setPlaybackSpeed(2.0f)
                            playbackSpeed = 2f
                            gestureText = "2x 加速"
                        },
                        onDoubleTap = {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            isPlaying = exoPlayer.isPlaying
                        }
                    )
                }
                .pointerInput(isLocked, duration) {
                    // 拖动手势
                    var startX = 0f
                    var startY = 0f
                    var startPositionLocal = 0L
                    var startBrightness = 0.5f
                    var startVolume = 0
                    var isDragging = false
                    var dragType = 0 // 1=seek, 2=brightness, 3=volume
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                    val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)

                    detectDragGestures(
                        onDragStart = { offset ->
                            startX = offset.x
                            startY = offset.y
                            startPositionLocal = exoPlayer.currentPosition
                            startBrightness = activity?.window?.attributes?.screenBrightness ?: 0.5f
                            if (startBrightness < 0) startBrightness = 0.5f
                            startVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                            isDragging = false
                            dragType = 0
                            if (playbackSpeed != 1f) {
                                exoPlayer.setPlaybackSpeed(1.0f)
                                playbackSpeed = 1f
                                gestureText = ""
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val dx = change.position.x - startX
                            val dy = change.position.y - startY
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()

                            if (!isDragging && (kotlin.math.abs(dx) > 20 || kotlin.math.abs(dy) > 20)) {
                                isDragging = true
                                dragType = when {
                                    w > 0 && kotlin.math.abs(dx) > kotlin.math.abs(dy) -> 1
                                    w > 0 && startX < w / 2 -> 2
                                    else -> 3
                                }
                            }
                            if (isDragging) {
                                when (dragType) {
                                    1 -> {
                                        val dur = exoPlayer.duration.coerceAtLeast(1L)
                                        val delta = (dx / w * dur * 0.5).toLong()
                                        val newPos = (startPositionLocal + delta).coerceIn(0, dur)
                                        exoPlayer.seekTo(newPos)
                                        gestureText = "${if (delta >= 0) "+" else ""}${delta / 1000}s"
                                    }
                                    2 -> {
                                        val delta = -(dy / h)
                                        val newBr = (startBrightness + delta).coerceIn(0.01f, 1f)
                                        activity?.window?.attributes = activity?.window?.attributes?.apply { screenBrightness = newBr }
                                        gestureText = "亮度 ${(newBr * 100).toInt()}%"
                                    }
                                    3 -> {
                                        val delta = -(dy / h * maxVolume).toInt()
                                        val newVol = (startVolume + delta).coerceIn(0, maxVolume)
                                        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVol, 0)
                                        gestureText = "音量 ${newVol * 100 / maxVolume}%"
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            gestureText = ""
                        }
                    )
                }
        )

        // 手势提示
        if (gestureText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(text = gestureText, style = MiuixTheme.textStyles.body1, color = Color.White)
            }
        }

        // ====== 全屏控制层 ======
        AnimatedVisibility(
            visible = showControls && !isLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 顶部栏：返回 + 标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberVectorPainter(IconBack),
                        contentDescription = "退出全屏",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(22.dp).clickable(onClick = onExitFullscreen)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.body2,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }

                // 右侧：锁屏按钮
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .clickable { isLocked = !isLocked },
                    contentAlignment = Alignment.Center
                ) {
                    val lockIcon = if (isLocked) IconFloating else IconLock
                    Image(
                        painter = rememberVectorPainter(lockIcon),
                        contentDescription = if (isLocked) "解锁" else "锁屏",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 中央：播放/暂停
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .clickable {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            isPlaying = exoPlayer.isPlaying
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberVectorPainter(if (isPlaying) IconPause else IconPlay),
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // ====== 底部控制区 ======
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // 进度条行：当前时间 | 进度条 | 总时长
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 当前时间
                        Text(
                            text = fmtTime(currentPosition),
                            style = MiuixTheme.textStyles.footnote1,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        // 可点击进度条
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .pointerInput(duration) {
                                    detectTapGestures { offset ->
                                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                        exoPlayer.seekTo((fraction * duration).toLong())
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                            // 背景轨道
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
                            )
                            // 已播放部分
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White)
                                    .align(Alignment.CenterStart)
                            )
                            // 进度圆点（用 Box 模拟，通过 padding 定位）
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterStart)
                                    .padding(start = (maxWidth * progress).coerceAtMost(maxWidth - 6.dp)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.White, RoundedCornerShape(50))
                                )
                            }
                        }

                        // 总时长
                        Text(
                            text = fmtTime(duration),
                            style = MiuixTheme.textStyles.footnote1,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 功能按钮行：播放控制 | 倍速 + 退出全屏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberVectorPainter(if (isPlaying) IconPause else IconPlay),
                                contentDescription = if (isPlaying) "暂停" else "播放",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                        isPlaying = exoPlayer.isPlaying
                                    }
                                    .padding(4.dp)
                            )
                            Image(
                                painter = rememberVectorPainter(IconNext),
                                contentDescription = "下一集",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                            )
                        }

                        // 右侧按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 倍速
                            Text(
                                text = "${playbackSpeed}x",
                                style = MiuixTheme.textStyles.footnote2,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .clickable {
                                        val next = when (playbackSpeed) {
                                            1f -> 1.5f; 1.5f -> 2f; 2f -> 3f; else -> 1f
                                        }
                                        playbackSpeed = next
                                        exoPlayer.setPlaybackSpeed(next)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                            // 分辨率/超分
                            Image(
                                painter = rememberVectorPainter(IconHD),
                                contentDescription = "超分",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.size(28.dp).padding(2.dp)
                            )
                            // 退出全屏
                            Image(
                                painter = rememberVectorPainter(IconExitFullscreen),
                                contentDescription = "退出全屏",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable(onClick = onExitFullscreen)
                            )
                        }
                    }
                }
            }
        }

        // 锁屏状态：只显示解锁按钮
        if (isLocked) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .clickable { isLocked = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberVectorPainter(IconLock),
                            contentDescription = "解锁",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun fmtTime(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
