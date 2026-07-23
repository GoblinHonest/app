/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.util

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS 交互式返回由导航容器处理
}
