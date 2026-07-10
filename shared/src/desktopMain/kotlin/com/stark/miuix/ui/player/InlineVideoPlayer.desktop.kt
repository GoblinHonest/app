package com.stark.miuix.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
actual fun InlineVideoPlayer(
    url: String,
    title: String,
    modifier: Modifier,
    onRequestFullscreen: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onPositionChanged: (Long) -> Unit,
    isFullscreen: Boolean
) {
    Box(modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black), contentAlignment = Alignment.Center) {
        Text(text = title, style = MiuixTheme.textStyles.body2, color = Color.White)
    }
}
