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

package com.stark.miuix.data.model

import kotlinx.serialization.Serializable

/**
 * 字幕数据模型
 *
 * 支持内嵌字幕（视频流自带）、外挂字幕（本地文件）和在线字幕。
 *
 * @property name 字幕名称/语言标识
 * @property url 字幕文件 URL 或本地路径
 * @property language 语言代码（如 zh-CN, en, ja）
 * @property format 字幕格式：srt / ass / vtt
 * @property isLocal 是否为本地文件
 */
@Serializable
data class Subtitle(
    val name: String = "",
    val url: String = "",
    val language: String = "",
    val format: String = "srt",
    val isLocal: Boolean = false
)
