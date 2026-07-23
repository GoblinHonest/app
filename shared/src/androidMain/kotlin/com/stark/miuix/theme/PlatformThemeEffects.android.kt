/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Android：根据暗色模式切换状态栏/导航栏图标颜色。
 */
@Composable
actual fun PlatformThemeEffects(isDark: Boolean) {
    val view = LocalView.current
    SideEffect {
        val context = view.context
        val window = (context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
    }
}
