/*
 * Copyright 2024 Stark Industries
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stark.miuix.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * 剪贴板工具
 *
 * 基于 Compose 的 [LocalClipboardManager] 实现跨平台剪贴板操作，
 * 无需 expect/actual — Compose Multiplatform 已统一各平台实现。
 */
object ClipboardUtils {

    /**
     * 获取剪贴板复制函数
     *
     * 在 @Composable 上下文中调用，返回一个可在任何地方执行的 lambda。
     */
    @Composable
    fun rememberCopyAction(): (String) -> Unit {
        val clipboardManager = LocalClipboardManager.current
        return { text: String ->
            clipboardManager.setText(AnnotatedString(text))
        }
    }
}
