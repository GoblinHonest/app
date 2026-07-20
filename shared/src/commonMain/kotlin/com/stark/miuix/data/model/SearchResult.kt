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
 * 搜索结果数据模型
 *
 * 搜索引擎返回的单条搜索结果。
 *
 * @property title 视频标题
 * @property cover 封面图片 URL
 * @property url 详情页 URL
 * @property description 描述信息（如类型、年份等）
 * @property sourceName 搜索结果来源名称
 */
@Serializable
data class SearchResult(
    val title: String = "",
    val cover: String = "",
    val url: String = "",
    val description: String = "",
    val sourceName: String = "",
    val category: String = "",
    val score: Double = 0.0
)
