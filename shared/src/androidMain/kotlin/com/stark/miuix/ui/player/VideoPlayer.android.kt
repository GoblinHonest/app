package com.stark.miuix.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs

/**
 * Android 播放器 — 自定义控制 UI + 手势
 *
 * 控制层：
 * - 顶部：返回 + 标题 + 倍速
 * - 中央：播放/暂停按钮
 * - 底部：进度条 + 时间
 *
 * 手势：
 * - 左侧上下滑：亮度
 * - 右侧上下滑：音量
 * - 横滑：进度
 * - 长按：2x 加速
 * - 点击：显示/隐藏控制层
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
actual fun VideoPlayer(
    url: String,
    title: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }
    var gestureText by remember { mutableStateOf("") }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var dmEnabled by remember { mutableStateOf(false) }   // 弹幕开关 (逆向: dm_open/dm_close)
    var isLocked by remember { mutableStateOf(false) }    // 锁屏 (逆向: lock/unlock)

    // 全屏 + 隐藏系统栏
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.decorView?.windowInsetsController?.let { c ->
            c.hide(WindowInsets.Type.systemBars())
            c.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.decorView?.windowInsetsController?.show(WindowInsets.Type.systemBars())
        }
    }

    // 定时刷新进度
    LaunchedEffect(isPlaying) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(1L)
            isPlaying = exoPlayer.isPlaying
            delay(500)
        }
    }

    // 3 秒自动隐藏控制层
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // PlayerView（无默认控制器）
        AndroidView(
            factory = { ctx ->
                val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false

                    var isLongPress = false
                    var startX = 0f
                    var startY = 0f
                    var startPosition = 0L
                    var startBrightness = 0f
                    var startVolume = 0
                    var isDragging = false
                    var dragType = 0

                    val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            showControls = !showControls
                            return true
                        }
                        override fun onLongPress(e: MotionEvent) {
                            isLongPress = true
                            exoPlayer.setPlaybackSpeed(2.0f)
                            playbackSpeed = 2f
                            gestureText = "2x 加速"
                        }
                    })

                    setOnTouchListener { v, event ->
                        gestureDetector.onTouchEvent(event)
                        val viewWidth = v.width.toFloat()
                        val viewHeight = v.height.toFloat()

                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                startX = event.x
                                startY = event.y
                                startPosition = exoPlayer.currentPosition
                                startBrightness = activity?.window?.attributes?.screenBrightness ?: 0.5f
                                if (startBrightness < 0) startBrightness = 0.5f
                                startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                isDragging = false
                                dragType = 0
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val dx = event.x - startX
                                val dy = event.y - startY
                                if (!isDragging && (abs(dx) > 30 || abs(dy) > 30)) {
                                    isDragging = true
                                    dragType = if (abs(dx) > abs(dy)) 1
                                               else if (startX < viewWidth / 2) 2 else 3
                                }
                                if (isDragging) {
                                    when (dragType) {
                                        1 -> {
                                            val dur = exoPlayer.duration.coerceAtLeast(1L)
                                            val seekDelta = (dx / viewWidth * dur * 0.5).toLong()
                                            val newPos = (startPosition + seekDelta).coerceIn(0, dur)
                                            exoPlayer.seekTo(newPos)
                                            gestureText = "${if (seekDelta >= 0) "+" else ""}${seekDelta / 1000}s"
                                        }
                                        2 -> {
                                            val delta = -(dy / viewHeight)
                                            val newBr = (startBrightness + delta).coerceIn(0.01f, 1f)
                                            activity?.window?.attributes = activity?.window?.attributes?.apply { screenBrightness = newBr }
                                            gestureText = "亮度 ${(newBr * 100).toInt()}%"
                                        }
                                        3 -> {
                                            val delta = -(dy / viewHeight * maxVolume).toInt()
                                            val newVol = (startVolume + delta).coerceIn(0, maxVolume)
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                            gestureText = "音量 ${newVol * 100 / maxVolume}%"
                                        }
                                    }
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                if (isLongPress) {
                                    isLongPress = false
                                    exoPlayer.setPlaybackSpeed(1.0f)
                                    playbackSpeed = 1f
                                }
                                isDragging = false
                                gestureText = ""
                            }
                        }
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
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

        // 自定义控制层 — 对标优酷全屏播放器设计图
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 顶部栏：← 返回 | 标题 | 倍速
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "←",
                        style = MiuixTheme.textStyles.headline1,
                        color = Color.White,
                        modifier = Modifier.clickable { activity?.onBackPressed() }
                    )
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.body2,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    // 倍速按钮（对标设计图右侧操作区）
                    listOf("倍速", "选集").forEach { label ->
                        Text(
                            text = if (label == "倍速") "${playbackSpeed}x" else label,
                            style = MiuixTheme.textStyles.footnote1,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .clickable {
                                    if (label == "倍速") {
                                        val next = when (playbackSpeed) {
                                            1f -> 1.5f; 1.5f -> 2f; 2f -> 3f; else -> 1f
                                        }
                                        playbackSpeed = next
                                        exoPlayer.setPlaybackSpeed(next)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 右侧：锁屏按钮（设计图右侧圆形按钮）
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { /* 锁屏功能 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔓", style = MiuixTheme.textStyles.body2, color = Color.White)
                }

                // 中央播放/暂停（双击区域）
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50))
                        .clickable {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            isPlaying = exoPlayer.isPlaying
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        style = MiuixTheme.textStyles.headline1,
                        color = Color.White
                    )
                }

                // 底部控制区 — 完整对标设计图
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // 进度条行：时间 | 进度条 | 总时长
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            style = MiuixTheme.textStyles.footnote1,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        // 进度条（带拖动圆点）
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(1.5.dp))
                            )
                            val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(3.dp)
                                    .background(Color.White, RoundedCornerShape(1.5.dp))
                                    .align(Alignment.CenterStart)
                            )
                            // 进度点
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = (progress * 100).coerceIn(0f, 100f).dp.coerceAtMost(2000.dp))
                                    .size(12.dp)
                                    .background(Color.White, RoundedCornerShape(50))
                            )
                        }
                        Text(
                            text = formatTime(duration),
                            style = MiuixTheme.textStyles.footnote1,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // 功能栏：▶ | ⏭ | 弹幕 ··· 超分辨率 | 倍速 | 选集
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ▶/⏸ 播放暂停 (逆向: play/pause)
                        Text(
                            text = if (isPlaying) "⏸" else "▶",
                            style = MiuixTheme.textStyles.body1,
                            color = Color.White,
                            modifier = Modifier.clickable {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        // ⏭ 下一集 (逆向: next)
                        Text(
                            text = "⏭",
                            style = MiuixTheme.textStyles.body1,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        // 弹幕开关 (逆向: dm_open / dm_close)
                        Text(
                            text = if (dmEnabled) "弹" else "弹",
                            style = MiuixTheme.textStyles.footnote2,
                            color = if (dmEnabled) Color(0xFF3D7BF9) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .background(
                                    if (dmEnabled) Color(0xFF3D7BF9).copy(alpha = 0.2f)
                                    else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { dmEnabled = !dmEnabled }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // 超分辨率 (逆向: Anime4K shaders)
                        Text(
                            text = "超分",
                            style = MiuixTheme.textStyles.footnote2,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                        // 倍速 (逆向: clock/settings)
                        Text(
                            text = "${playbackSpeed}x",
                            style = MiuixTheme.textStyles.footnote2,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .clickable {
                                    val next = when (playbackSpeed) {
                                        1f -> 1.5f; 1.5f -> 2f; 2f -> 3f; else -> 1f
                                    }
                                    playbackSpeed = next
                                    exoPlayer.setPlaybackSpeed(next)
                                }
                                .padding(horizontal = 5.dp)
                        )
                        // 悬浮窗 (逆向: floating)
                        Text(
                            text = "悬浮",
                            style = MiuixTheme.textStyles.footnote2,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                        // 选集 (逆向: arc/book)
                        Text(
                            text = "选集",
                            style = MiuixTheme.textStyles.footnote2,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
