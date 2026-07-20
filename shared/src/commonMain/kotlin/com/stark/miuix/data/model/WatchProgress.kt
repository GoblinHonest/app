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
 * 观看进度数据模型
 *
 * 记录用户在某个视频某集的精确播放位置，用于断点续播。
 * 播放过程中每 5 秒持久化一次。
 *
 * @property videoId 视频唯一标识
 * @property episodeIndex 当前集数索引
 * @property episodeName 当前集名称
 * @property positionMs 当前播放位置（毫秒）
 * @property durationMs 总时长（毫秒）
 * @property timestamp 最后更新时间戳
 * @property sourceName 视频源名称
 * @property detailUrl 详情页地址
 * @property title 视频标题
 * @property cover 封面 URL
 */
@Serializable
data class WatchProgress(
    val videoId: String = "",
    val episodeIndex: Int = 0,
    val episodeName: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val timestamp: Long = 0L,
    val sourceName: String = "",
    val detailUrl: String = "",
    val title: String = "",
    val cover: String = ""
) {
    /** 播放进度百分比 (0.0 ~ 1.0) */
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    /** 是否已看完（进度 > 95%） */
    val isCompleted: Boolean
        get() = progressFraction > 0.95f
}
