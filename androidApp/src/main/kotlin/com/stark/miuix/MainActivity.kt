package com.stark.miuix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stark.miuix.data.storage.androidDataDir

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidDataDir = filesDir.absolutePath
        enableEdgeToEdge()
        setContent { App() }
    }
}
