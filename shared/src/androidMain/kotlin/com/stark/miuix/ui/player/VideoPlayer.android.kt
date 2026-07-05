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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconDanmaku
import com.stark.miuix.ui.icons.IconDanmakuOff
import com.stark.miuix.ui.icons.IconFloating
import com.stark.miuix.ui.icons.IconFullscreen
import com.stark.miuix.ui.icons.IconHD
import com.stark.miuix.ui.icons.IconLock
import com.stark.miuix.ui.icons.IconMore
import com.stark.miuix.ui.icons.IconNext
import com.stark.miuix.ui.icons.IconPause
import com.stark.miuix.ui.icons.IconPlay
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.stark.miuix.util.VideoPlayerState
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

    // HyperOS L1 适配：MediaSession — 锁屏/通知栏/蓝牙耳机播控
    val mediaSession = remember(context, exoPlayer) {
        androidx.media3.session.MediaSession.Builder(context, exoPlayer)
            .setId("cinehub_player")
            .build()
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
            VideoPlayerState.isPlaying = false  // 退出播放，禁止 PiP 触发
            mediaSession.release()
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.decorView?.windowInsetsController?.show(WindowInsets.Type.systemBars())
        }
    }

    // HyperOS 多窗口适配：监听 onStop/onStart（不在 onPause 停止播放）
    // 文档要求: 多窗口失去焦点时不应停止播放，只在 onStop 时暂停
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                Lifecycle.Event.ON_START -> if (isPlaying) exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 定时刷新进度
    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(1L)
            isPlaying = exoPlayer.isPlaying
            VideoPlayerState.isPlaying = exoPlayer.isPlaying  // 同步全局 PiP 状态
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

        // 自定义控制层 — 锁屏时仅显示解锁按钮
        AnimatedVisibility(
            visible = showControls && !isLocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 顶部栏：← 返回 | 标题 | 更多
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
                    // ← 返回 (逆向: video_back.svg)
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconBack),
                        contentDescription = "返回",
                        modifier = Modifier.size(22.dp).clickable { activity?.onBackPressed() }
                    )
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.body2,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    // 倍速只在底部控制栏显示，顶部仅保留标题，避免重复
                }

                // 右侧：锁屏按钮（设计图右侧圆形按钮）
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { isLocked = !isLocked },
                    contentAlignment = Alignment.Center
                ) {
                    // lock/unlock SVG — 修复 Bug：两分支相同
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                            if (isLocked) IconLock else com.stark.miuix.ui.icons.IconFloating
                        ),
                        contentDescription = if (isLocked) "解锁" else "锁屏",
                        modifier = Modifier.size(20.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                    )
                }

                // 中央播放/暂停 — SVG图标 (逆向: play.svg / pause.svg)
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
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                            if (isPlaying) IconPause else IconPlay
                        ),
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(28.dp)
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
                        // 进度条 — BoxWithConstraints 获取实际宽度，点击跳进度
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .pointerInput(duration) {
                                    // 点击进度条精确跳转
                                    detectTapGestures { offset ->
                                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                        exoPlayer.seekTo((fraction * duration).toLong())
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val barWidth = maxWidth
                            val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(1.5.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(3.dp)
                                    .background(Color.White, RoundedCornerShape(1.5.dp))
                                    .align(Alignment.CenterStart)
                            )
                            // 进度点 — 准确定位：progress * 实际宽度
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = (barWidth * progress).coerceAtMost(barWidth - 6.dp))
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
                    // 功能栏 — 完全使用SVG图标（逆向APK icons/*.svg）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // play/pause SVG (逆向: play.svg/pause.svg)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                if (isPlaying) IconPause else IconPlay
                            ),
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            modifier = Modifier.size(32.dp).clickable {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        // next SVG (逆向: next.svg)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconNext),
                            contentDescription = "下一集",
                            modifier = Modifier.size(32.dp).padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        // 弹幕 SVG (逆向: dm_open.svg / dm_close.svg)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                if (dmEnabled) IconDanmaku else IconDanmakuOff
                            ),
                            contentDescription = "弹幕",
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                if (dmEnabled) Color(0xFF3D7BF9) else Color.White.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (dmEnabled) Color(0xFF3D7BF9).copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { dmEnabled = !dmEnabled }
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // HD SVG (逆向: Anime4K 超分辨率)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconHD),
                            contentDescription = "超分",
                            modifier = Modifier.size(28.dp).padding(horizontal = 6.dp)
                        )
                        // 倍速文字（保持文字，逆向APK也用文字标签）
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
                        // 悬浮 SVG (逆向: floating.svg)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconFloating),
                            contentDescription = "悬浮",
                            modifier = Modifier.size(28.dp).padding(horizontal = 6.dp)
                        )
                        // 全屏 SVG (逆向: full.svg)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconFullscreen),
                            contentDescription = "全屏",
                            modifier = Modifier.size(28.dp).padding(start = 6.dp)
                        )
                    }
                }
            }
        }

        // 锁屏状态：只显示解锁按钮，点击解锁
        if (isLocked) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .clickable { isLocked = false },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(IconLock),
                            contentDescription = "解锁",
                            modifier = Modifier.size(22.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
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
