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

import androidx.compose.ui.graphics.vector.ImageVector
import com.stark.miuix.ui.icons.IconHome
import com.stark.miuix.ui.icons.IconSearch
import com.stark.miuix.ui.icons.IconUser
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
 * 底部导航 Tab — 对标逆向 APK 图标集
 *
 * 使用自绘 SVG 路径图标，对应逆向提取的 home.svg / search.svg / user.svg
 */
enum class BottomTab(val label: String, val imageVector: ImageVector, val screen: Screen) {
    HOME("首页", IconHome, Screen.Home),
    SEARCH("搜索", IconSearch, Screen.Search),
    PROFILE("我的", IconUser, Screen.Profile)
}
