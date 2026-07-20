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

package com.stark.miuix.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Desktop 平台播放器实现
 *
 * 当前为基础实现，显示播放信息。
 * 后续可集成 JavaFX Media 或 VLCJ 实现完整播放能力。
 *
 * @param url 视频 URL
 * @param title 视频标题
 * @param modifier Modifier
 */
@Composable
actual fun VideoPlayer(
    url: String,
    title: String,
    modifier: Modifier,
    startPosition: Long
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNotBlank()) {
            Text(
                text = "正在播放: $title\n$url",
                style = MiuixTheme.textStyles.body1,
                color = Color.White
            )
        } else {
            Text(
                text = "无可用播放地址",
                style = MiuixTheme.textStyles.body1,
                color = Color.White
            )
        }
    }
}
