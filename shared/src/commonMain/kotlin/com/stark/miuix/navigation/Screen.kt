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
 * 使用密封类 + @Serializable 实现类型安全导航。
 * Navigation Compose 通过泛型 composable<T> 匹配路由，
 * 无需手动定义 route 字符串。
 */
sealed class Screen {

    /** 首页 */
    @Serializable
    data object Home : Screen()

    /** 分类页 */
    @Serializable
    data class Category(
        val sourceName: String,
        val categoryUrl: String = ""
    ) : Screen()

    /** 搜索页 */
    @Serializable
    data object Search : Screen()

    /** 我的 */
    @Serializable
    data object Profile : Screen()

    /** 视频详情页 */
    @Serializable
    data class Detail(
        val sourceName: String,
        val detailUrl: String,
        val title: String = "",
        val coverUrl: String = ""
    ) : Screen()

    /** 播放页 */
    @Serializable
    data class Player(
        val sourceName: String,
        val episodeUrl: String,
        val title: String = ""
    ) : Screen()

    /** 视频源管理 */
    @Serializable
    data object SourceManage : Screen()

    /** 设置页 */
    @Serializable
    data object Settings : Screen()
}

/**
 * 底部导航栏 Tab 定义
 *
 * @property label Tab 显示文字
 * @property screen 对应的目标页面
 */
/**
 * 底部导航 Tab 定义
 *
 * icon 使用语义清晰的 Unicode 符号，对标 Bangumi App 图标风格：
 * - 首页：方形播放框
 * - 搜索：圆形搜索
 * - 我的：人形轮廓
 */
enum class BottomTab(val label: String, val icon: String, val screen: Screen) {
    HOME("首页", "⬛", Screen.Home),
    SEARCH("搜索", "🔍", Screen.Search),
    PROFILE("我的", "👤", Screen.Profile)
}
