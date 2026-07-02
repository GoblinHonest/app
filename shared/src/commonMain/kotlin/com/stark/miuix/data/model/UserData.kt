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
 * 观看历史记录
 *
 * @property videoId 视频唯一标识
 * @property title 视频标题
 * @property cover 封面 URL
 * @property sourceName 视频源名称
 * @property detailUrl 详情页地址
 * @property lastEpisode 最后观看的剧集名称
 * @property progress 观看进度（0.0-1.0）
 * @property timestamp 最后观看时间戳
 */
@Serializable
data class WatchHistory(
    val videoId: String,
    val title: String,
    val cover: String,
    val sourceName: String,
    val detailUrl: String,
    val lastEpisode: String = "",
    val progress: Float = 0f,
    val timestamp: Long = 0L
)

/**
 * 收藏记录
 *
 * @property videoId 视频唯一标识
 * @property title 视频标题
 * @property cover 封面 URL
 * @property sourceName 视频源名称
 * @property detailUrl 详情页地址
 * @property timestamp 收藏时间戳
 */
@Serializable
data class Favorite(
    val videoId: String,
    val title: String,
    val cover: String,
    val sourceName: String,
    val detailUrl: String,
    val timestamp: Long = 0L
)
