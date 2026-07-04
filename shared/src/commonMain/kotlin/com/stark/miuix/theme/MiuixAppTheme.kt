/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.stark.miuix.ui.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 全局主题状态 — 简化版
 *
 * 只支持亮色/暗色两种模式，默认亮色。
 */
object ThemeState {
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }
}

/**
 * 应用主题 — 亮色/暗色切换
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val themeMode by ThemeState.themeMode.collectAsState()

    val colorSchemeMode = when (themeMode) {
        ThemeMode.LIGHT -> ColorSchemeMode.Light
        ThemeMode.DARK -> ColorSchemeMode.Dark
    }

    val controller = remember(colorSchemeMode) {
        ThemeController(colorSchemeMode = colorSchemeMode)
    }

    MiuixTheme(controller = controller, content = content)
}
