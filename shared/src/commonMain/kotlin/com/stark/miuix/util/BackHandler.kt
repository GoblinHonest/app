/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.util

import androidx.compose.runtime.Composable

/**
 * 跨平台返回拦截。
 *
 * Android：系统返回键 / 手势返回。
 * 其他平台：可映射 ESC 等（默认空实现）。
 */
@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)
