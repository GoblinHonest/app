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
 * 视频数据模型
 *
 * 表示一个视频内容的完整信息，包括基本元数据和剧集列表。
 *
 * @property id 视频唯一标识（通常为详情页 URL 的哈希值）
 * @property title 视频标题
 * @property cover 封面图片 URL
 * @property description 视频简介
 * @property status 状态（连载中/已完结）
 * @property sourceName 来源名称
 * @property detailUrl 详情页 URL
 * @property episodes 剧集列表
 * @property category 所属分类
 * @property updateTime 最近更新时间
 */
@Serializable
data class Video(
    val id: String = "",
    val title: String = "",
    val cover: String = "",
    val description: String = "",
    val status: String = "",
    val sourceName: String = "",
    val detailUrl: String = "",
    val episodes: List<Episode> = emptyList(),
    val category: String = "",
    val updateTime: String = ""
)
