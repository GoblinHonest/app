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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 跨平台视频播放器组件（expect 声明）
 *
 * 各平台通过 actual 实现提供原生播放能力：
 * - Android: ExoPlayer
 * - Desktop: VLC / JavaFX Media
 * - WasmJs: HTML5 Video
 * - iOS: AVPlayer
 *
 * @param url 视频播放地址
 * @param title 视频标题（用于播放器 UI 展示）
 * @param modifier Modifier 修饰符
 */
@Composable
expect fun VideoPlayer(
    url: String,
    title: String,
    modifier: Modifier
)
