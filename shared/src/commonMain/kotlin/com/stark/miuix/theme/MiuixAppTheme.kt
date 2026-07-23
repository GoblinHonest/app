/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.ui.theme.DesignTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/** 主题模式：跟随系统 / 亮色 / 暗色 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("亮色模式"),
    DARK("暗色模式");

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: LIGHT
    }
}

/** 当前是否为暗色界面（解析 SYSTEM 后的结果） */
val LocalAppIsDark = staticCompositionLocalOf { false }

/**
 * 全局主题状态
 *
 * 单一数据源，设置页与 AppTheme 共用；支持持久化到 [LocalStorage]。
 */
object ThemeState {
    private const val KEY_THEME_MODE = "theme_mode"

    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private var storage: LocalStorage? = null
    private var initialized = false

    /** 绑定存储并恢复上次选择（应用启动时调用一次） */
    fun init(localStorage: LocalStorage) {
        if (initialized && storage === localStorage) return
        storage = localStorage
        initialized = true
        val saved = localStorage.loadSettings()[KEY_THEME_MODE]
        _themeMode.value = ThemeMode.fromStorage(saved)
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        val store = storage ?: return
        val map = store.loadSettings().toMutableMap()
        map[KEY_THEME_MODE] = mode.name
        store.saveSettings(map)
    }
}

/**
 * 应用主题 — 支持跟随系统 / 亮色 / 暗色
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val themeMode by ThemeState.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val colorSchemeMode = if (isDark) ColorSchemeMode.Dark else ColorSchemeMode.Light
    val controller = remember(colorSchemeMode) {
        ThemeController(colorSchemeMode = colorSchemeMode)
    }

    CompositionLocalProvider(LocalAppIsDark provides isDark) {
        PlatformThemeEffects(isDark = isDark)
        MiuixTheme(controller = controller, content = content)
    }
}

/**
 * 主题自适应颜色
 *
 * 业务 UI 优先用这里的品牌色；图片叠层/播放器控件可继续用固定白字。
 */
object AppColors {
    /** 品牌主色：暗色提亮，亮色用标准蓝 */
    @Composable
    fun brand(): Color =
        if (LocalAppIsDark.current) DesignTokens.brandBlueDarkMode else DesignTokens.brandBlue

    /** 品牌浅色（渐变高光） */
    @Composable
    fun brandLight(): Color = DesignTokens.brandBlueLight

    /** 品牌色上的内容色（按钮选中文字等） */
    @Composable
    fun onBrand(): Color = Color.White

    /** 海报底部渐变末端：暗色略透，避免糊成死黑 */
    @Composable
    fun posterGradientEnd(): Color =
        if (LocalAppIsDark.current) Color(0xB3000000) else DesignTokens.posterGradientEnd

    /** Banner 底部渐变末端 */
    @Composable
    fun bannerGradientEnd(): Color =
        if (LocalAppIsDark.current) Color(0x99000000) else DesignTokens.bannerGradientEnd
}
