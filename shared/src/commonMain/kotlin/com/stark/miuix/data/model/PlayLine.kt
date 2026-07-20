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
 * 播放线路数据模型
 *
 * 一个视频源可能提供多条播放线路（如"线路1""量子云""非凡云"），
 * 每条线路有独立的剧集列表和请求头。
 *
 * @property name 线路名称
 * @property episodes 该线路下的剧集列表
 * @property playerHeaders 播放时需要的请求头
 */
@Serializable
data class PlayLine(
    val name: String = "",
    val episodes: List<Episode> = emptyList(),
    val playerHeaders: Map<String, String> = emptyMap()
)
