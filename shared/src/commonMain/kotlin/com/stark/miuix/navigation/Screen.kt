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

package com.stark.miuix.navigation

import kotlinx.serialization.Serializable

/**
 * 应用页面路由定义
 *
 * 使用密封类定义所有页面路由，类型安全且支持带参数导航。
 * 每个子对象/数据类对应一个页面，[route] 用于 Navigation Compose 路由匹配。
 */
sealed class Screen {

    /** 页面路由标识符 */
    abstract val route: String

    /**
     * 首页
     * 展示推荐视频、分类入口、搜索栏
     */
    @Serializable
    data object Home : Screen() {
        override val route = "home"
    }

    /**
     * 分类页
     * 按分类展示视频列表，支持 Tab 切换
     *
     * @property sourceName 视频源名称
     * @property categoryUrl 分类页面 URL
     */
    @Serializable
    data class Category(
        val sourceName: String,
        val categoryUrl: String = ""
    ) : Screen() {
        override val route = "category/{sourceName}/{categoryUrl}"
    }

    /**
     * 搜索页
     * 全局搜索视频，聚合多个视频源结果
     */
    @Serializable
    data object Search : Screen() {
        override val route = "search"
    }

    /**
     * 视频详情页
     * 展示视频信息、剧集列表
     *
     * @property sourceName 视频源名称
     * @property detailUrl 详情页 URL
     * @property title 视频标题（用于快速展示，无需等待加载）
     * @property coverUrl 封面图片 URL
     */
    @Serializable
    data class Detail(
        val sourceName: String,
        val detailUrl: String,
        val title: String = "",
        val coverUrl: String = ""
    ) : Screen() {
        override val route = "detail/{sourceName}/{detailUrl}"
    }

    /**
     * 播放页
     * 全屏视频播放器
     *
     * @property sourceName 视频源名称
     * @property episodeUrl 剧集 URL
     * @property title 视频标题
     */
    @Serializable
    data class Player(
        val sourceName: String,
        val episodeUrl: String,
        val title: String = ""
    ) : Screen() {
        override val route = "player/{sourceName}/{episodeUrl}"
    }

    /**
     * 视频源管理页
     * 导入/导出/启用/禁用视频源
     */
    @Serializable
    data object SourceManage : Screen() {
        override val route = "source_manage"
    }

    /**
     * 设置页
     * 主题、缓存、播放器等设置项
     */
    @Serializable
    data object Settings : Screen() {
        override val route = "settings"
    }
}
