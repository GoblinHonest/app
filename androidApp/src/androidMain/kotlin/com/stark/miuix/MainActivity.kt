package com.stark.miuix

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stark.miuix.data.storage.androidDataDir
import com.stark.miuix.ui.player.PlayerStore
import com.stark.miuix.util.HyperOsAdapter
import com.stark.miuix.util.VideoPlayerState

/**
 * Android 主 Activity — CineHub
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidDataDir = filesDir.absolutePath
        HyperOsAdapter.initialize(this)
        enableEdgeToEdge()

        // 全局崩溃日志
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH", "未捕获异常 [${thread.name}]", throwable)
            // 交给系统默认处理
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }

        setContent { App() }
    }

    private val defaultExceptionHandler by lazy {
        Thread.getDefaultUncaughtExceptionHandler()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * 画中画适配：只有在视频实际播放时才进入 PiP。
     * 多重校验：VideoPlayerState 标记 + ExoPlayer 存在且确实在播放。
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val player = PlayerStore.exoPlayer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && VideoPlayerState.isPlaying
            && player != null
            && player.isPlaying
        ) {
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(pipParams)
        }
    }
}
