/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop：依赖窗口级 ESC 处理时由宿主补充；此处保持 API 一致
    // 避免在无 focus 节点时误吞按键
}
