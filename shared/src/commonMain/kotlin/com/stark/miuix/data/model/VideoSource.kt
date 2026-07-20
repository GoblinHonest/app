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
 * 视频源数据模型
 *
 * 参考开源阅读（legado）的 bookSource 概念设计，
 * 定义了完整的视频源解析规则，包括搜索、分类、详情和播放规则。
 *
 * @property sourceName 源名称，用于在 UI 中展示
 * @property sourceUrl 源基础 URL
 * @property version 源版本号，用于更新检测
 * @property enabled 是否启用该源
 * @property searchRule 搜索规则
 * @property categoryRule 分类规则
 * @property detailRule 详情页解析规则
 * @property playerRule 播放地址解析规则
 */
@Serializable
data class VideoSource(
    val sourceName: String,
    val sourceUrl: String,
    val version: Int = 1,
    val enabled: Boolean = true,
    val sourceGroup: String = "",
    val sourceComment: String = "",
    val searchRule: SearchRule = SearchRule(),
    val categoryRule: CategoryRule = CategoryRule(),
    val detailRule: DetailRule = DetailRule(),
    val playerRule: PlayerRule = PlayerRule(),
    val lastCheckTime: Long = 0,
    val lastCheckStatus: String = "",
    val lastCheckLatencyMs: Long = 0
)

/**
 * 搜索规则
 *
 * @property searchUrl 搜索 URL 模板，使用 {{keyword}} 作为关键词占位符
 * @property ruleType 解析规则类型：xpath / css / jsonpath
 * @property listRule 列表项选择规则
 * @property titleRule 标题提取规则
 * @property coverRule 封面图片提取规则
 * @property urlRule 详情页链接提取规则
 * @property descRule 描述提取规则
 */
@Serializable
data class SearchRule(
    val searchUrl: String = "",
    val ruleType: String = "css",
    val listRule: String = "",
    val titleRule: String = "",
    val coverRule: String = "",
    val urlRule: String = "",
    val descRule: String = "",
    val suggestUrl: String = "",
    val suggestRule: String = ""
)

/**
 * 分类规则
 *
 * @property categoryUrl 分类页面 URL
 * @property ruleType 解析规则类型
 * @property categoryListRule 分类列表选择规则
 * @property categoryNameRule 分类名称提取规则
 * @property categoryUrlRule 分类链接提取规则
 * @property videoListRule 视频列表选择规则
 * @property titleRule 视频标题提取规则
 * @property coverRule 视频封面提取规则
 * @property urlRule 视频链接提取规则
 */
@Serializable
data class CategoryRule(
    val categoryUrl: String = "",
    val ruleType: String = "css",
    val categoryListRule: String = "",
    val categoryNameRule: String = "",
    val categoryUrlRule: String = "",
    val videoListRule: String = "",
    val titleRule: String = "",
    val coverRule: String = "",
    val urlRule: String = "",
    /** 大类 URL 映射，如 {"电视剧": "/api.php/...?t=2", "动漫": "/api.php/...?t=4"} */
    val categoryUrls: Map<String, String> = emptyMap()
)

/**
 * 详情页解析规则
 *
 * @property ruleType 解析规则类型
 * @property titleRule 标题提取规则
 * @property coverRule 封面提取规则
 * @property descRule 简介提取规则
 * @property statusRule 状态提取规则（连载中/已完结）
 * @property episodeListRule 剧集列表选择规则
 * @property episodeNameRule 剧集名称提取规则
 * @property episodeUrlRule 剧集播放链接提取规则
 */
@Serializable
data class DetailRule(
    val ruleType: String = "css",
    val titleRule: String = "",
    val coverRule: String = "",
    val descRule: String = "",
    val statusRule: String = "",
    val episodeListRule: String = "",
    val episodeNameRule: String = "",
    val episodeUrlRule: String = ""
)

/**
 * 播放规则
 *
 * @property ruleType 解析规则类型
 * @property playerUrlRule 播放地址提取规则
 * @property playerHeaders 播放请求头
 */
@Serializable
data class PlayerRule(
    val ruleType: String = "jsonpath",
    val playerUrlRule: String = "",
    val playerHeaders: Map<String, String> = emptyMap()
)
