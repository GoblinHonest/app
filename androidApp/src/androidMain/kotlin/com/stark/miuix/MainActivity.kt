package com.stark.miuix

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stark.miuix.data.storage.androidDataDir
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
        setContent { App() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * 画中画适配：只有在视频实际播放时才进入 PiP。
     * 通过 VideoPlayerState.isPlaying 全局标记判断。
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && VideoPlayerState.isPlaying
        ) {
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(pipParams)
        }
    }
}
