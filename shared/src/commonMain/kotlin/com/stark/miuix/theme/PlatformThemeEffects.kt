/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.theme

import androidx.compose.runtime.Composable

/**
 * 平台主题副作用：状态栏/导航栏图标亮暗适配等。
 *
 * @param isDark 当前是否暗色界面
 */
@Composable
expect fun PlatformThemeEffects(isDark: Boolean)
