package com.stark.miuix.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberShareAction(): (title: String, text: String) -> Unit =
    { _, _ -> }  // Desktop: no-op
