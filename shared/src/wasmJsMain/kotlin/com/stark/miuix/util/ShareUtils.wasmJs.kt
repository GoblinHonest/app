package com.stark.miuix.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberShareAction(): (title: String, text: String) -> Unit =
    { _, _ -> }  // WasmJs: no-op (use Web Share API if needed)
