package com.stark.miuix.util

import androidx.compose.runtime.Composable

/**
 * 原生分享功能（expect 声明）
 * Android: 调用系统分享弹窗（Intent.ACTION_SEND）
 * 其他平台: 复制到剪贴板
 */
@Composable
expect fun rememberShareAction(): (title: String, text: String) -> Unit
