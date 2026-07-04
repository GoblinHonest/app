package com.stark.miuix.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs

/**
 * Android 播放器 — 全屏沉浸式 + 原生手势控制
 *
 * 手势：
 * - 长按：2x 加速，松开恢复
 * - 左侧上下滑：调节亮度
 * - 右侧上下滑：调节音量
 * - 横向滑动：快进/快退
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

    var gestureText by remember { mutableStateOf("") }

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

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)

                        var isLongPress = false
                        var startX = 0f
                        var startY = 0f
                        var startPosition = 0L
                        var startBrightness = 0f
                        var startVolume = 0
                        var isDragging = false
                        var dragType = 0 // 0=none, 1=progress, 2=brightness, 3=volume

                        val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                            override fun onLongPress(e: MotionEvent) {
                                isLongPress = true
                                exoPlayer.setPlaybackSpeed(2.0f)
                                gestureText = "2x 加速中..."
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
                                                   else if (startX < viewWidth / 2) 2
                                                   else 3
                                    }

                                    if (isDragging) {
                                        when (dragType) {
                                            1 -> {
                                                // 横滑 — 进度
                                                val seekDelta = (dx / viewWidth * exoPlayer.duration * 0.5).toLong()
                                                val newPos = (startPosition + seekDelta).coerceIn(0, exoPlayer.duration)
                                                val sign = if (seekDelta >= 0) "+" else ""
                                                gestureText = "${sign}${seekDelta / 1000}s → ${formatTime(newPos)}"
                                            }
                                            2 -> {
                                                // 左侧上滑 — 亮度
                                                val delta = -(dy / viewHeight)
                                                val newBrightness = (startBrightness + delta).coerceIn(0.01f, 1f)
                                                activity?.window?.attributes = activity?.window?.attributes?.apply {
                                                    screenBrightness = newBrightness
                                                }
                                                gestureText = "亮度 ${(newBrightness * 100).toInt()}%"
                                            }
                                            3 -> {
                                                // 右侧上滑 — 音量
                                                val delta = -(dy / viewHeight * maxVolume).toInt()
                                                val newVolume = (startVolume + delta).coerceIn(0, maxVolume)
                                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                                                gestureText = "音量 ${(newVolume * 100 / maxVolume)}%"
                                            }
                                        }
                                    }
                                }

                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    if (isLongPress) {
                                        isLongPress = false
                                        exoPlayer.setPlaybackSpeed(1.0f)
                                    }
                                    if (isDragging && dragType == 1) {
                                        val dx = event.x - startX
                                        val seekDelta = (dx / viewWidth * exoPlayer.duration * 0.5).toLong()
                                        val newPos = (startPosition + seekDelta).coerceIn(0, exoPlayer.duration)
                                        exoPlayer.seekTo(newPos)
                                    }
                                    isDragging = false
                                    gestureText = ""
                                }
                            }
                            // 不消费事件，让 PlayerView 的控制器也能工作
                            false
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 手势提示文字
            if (gestureText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp)
                        .background(Color.Black.copy(alpha = 0.7f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = gestureText, style = MiuixTheme.textStyles.body1, color = Color.White)
                }
            }
        } else {
            Text(text = "无可用播放地址", style = MiuixTheme.textStyles.body1, color = Color.White)
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}
