package com.stark.miuix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stark.miuix.data.storage.androidDataDir

/**
 * Android 主 Activity
 *
 * Kamel 1.0.6 在检测到 OkHttp 引擎时自动使用，无需手动配置。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidDataDir = filesDir.absolutePath
        enableEdgeToEdge()
        setContent { App() }
    }
}
