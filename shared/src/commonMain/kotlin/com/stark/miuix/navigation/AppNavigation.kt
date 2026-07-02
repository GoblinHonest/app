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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.category.CategoryScreen
import com.stark.miuix.ui.detail.DetailScreen
import com.stark.miuix.ui.home.HomeScreen
import com.stark.miuix.ui.player.PlayerScreen
import com.stark.miuix.ui.search.SearchScreen
import com.stark.miuix.ui.settings.SettingsScreen
import com.stark.miuix.ui.source.SourceManageScreen

/**
 * 应用导航图
 *
 * 基于 Navigation Compose 的导航管理，定义所有页面的路由和转场动画。
 * 使用类型安全路由（[Screen] 导航类）替代字符串路由。
 *
 * 转场动画说明：
 * - 进入页面：从右侧滑入 + 淡入
 * - 退出页面：淡出
 * - 动画时长：300ms
 *
 * @param navController 导航控制器
 * @param videoRepository 视频仓库实例
 * @param sourceRepository 视频源仓库实例
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200))
        }
    ) {
        // 首页
        composable<Screen.Home> {
            HomeScreen(
                videoRepository = videoRepository,
                sourceRepository = sourceRepository,
                onNavigateToSearch = { navController.navigate(Screen.Search) },
                onNavigateToCategory = { sourceName, categoryUrl ->
                    navController.navigate(Screen.Category(sourceName, categoryUrl))
                },
                onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                    navController.navigate(Screen.Detail(sourceName, detailUrl, title, coverUrl))
                },
                onNavigateToSourceManage = { navController.navigate(Screen.SourceManage) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) }
            )
        }

        // 分类页
        composable<Screen.Category> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Category>()
            CategoryScreen(
                sourceName = args.sourceName,
                categoryUrl = args.categoryUrl,
                videoRepository = videoRepository,
                sourceRepository = sourceRepository,
                onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                    navController.navigate(Screen.Detail(sourceName, detailUrl, title, coverUrl))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 搜索页
        composable<Screen.Search> {
            SearchScreen(
                videoRepository = videoRepository,
                sourceRepository = sourceRepository,
                onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                    navController.navigate(Screen.Detail(sourceName, detailUrl, title, coverUrl))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 详情页
        composable<Screen.Detail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Detail>()
            DetailScreen(
                sourceName = args.sourceName,
                detailUrl = args.detailUrl,
                initialTitle = args.title,
                initialCoverUrl = args.coverUrl,
                videoRepository = videoRepository,
                sourceRepository = sourceRepository,
                onNavigateToPlayer = { sourceName, episodeUrl, title ->
                    navController.navigate(Screen.Player(sourceName, episodeUrl, title))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 播放页
        composable<Screen.Player> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Player>()
            PlayerScreen(
                sourceName = args.sourceName,
                episodeUrl = args.episodeUrl,
                title = args.title,
                videoRepository = videoRepository,
                sourceRepository = sourceRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 视频源管理
        composable<Screen.SourceManage> {
            SourceManageScreen(
                sourceRepository = sourceRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 设置页
        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
