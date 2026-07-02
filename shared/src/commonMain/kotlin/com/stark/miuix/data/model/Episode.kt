/*
 * Copyright 2024 Starter
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
 * 剧集数据模型
 *
 * 表示视频中的一个剧集/集数。
 *
 * @property name 剧集名称（如"第1集"、"EP01"）
 * @property url 剧集播放页面 URL
 * @property index 剧集排序索引
 */
@Serializable
data class Episode(
    val name: String = "",
    val url: String = "",
    val index: Int = 0
)
