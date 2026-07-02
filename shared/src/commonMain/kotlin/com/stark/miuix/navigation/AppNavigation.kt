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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.ui.category.CategoryScreen
import com.stark.miuix.ui.detail.DetailScreen
import com.stark.miuix.ui.home.HomeScreen
import com.stark.miuix.ui.player.PlayerScreen
import com.stark.miuix.ui.profile.ProfileScreen
import com.stark.miuix.ui.search.SearchScreen
import com.stark.miuix.ui.settings.SettingsScreen
import com.stark.miuix.ui.source.SourceManageScreen
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem

/**
 * 应用导航图
 *
 * 主容器结构：
 * - NavHost 承载所有页面
 * - BottomBar 仅在主 Tab 页面可见
 * - 转场动画：左滑进入 + 淡出退出
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    videoRepository: VideoRepository,
    sourceRepository: SourceRepository,
    userDataRepository: UserDataRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val mainTabClassNames = remember {
        BottomTab.entries.map { it.screen::class.qualifiedName.orEmpty() }
    }

    val isMainTab by remember {
        derivedStateOf {
            val currentRoute = navBackStackEntry?.destination?.route
            currentRoute != null && mainTabClassNames.any { name ->
                currentRoute.contains(name)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 导航内容区域
        Box(modifier = Modifier.weight(1f)) {
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
                        onNavigateToSourceManage = { navController.navigate(Screen.SourceManage) }
                    )
                }

                composable<Screen.Search> {
                    SearchScreen(
                        onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                            navController.navigate(Screen.Detail(sourceName, detailUrl, title, coverUrl))
                        }
                    )
                }

                composable<Screen.Profile> {
                    ProfileScreen(
                        userDataRepository = userDataRepository,
                        onNavigateToSourceManage = { navController.navigate(Screen.SourceManage) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings) },
                        onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                            navController.navigate(Screen.Detail(sourceName, detailUrl, title, coverUrl))
                        }
                    )
                }

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

                composable<Screen.Detail> { backStackEntry ->
                    val args = backStackEntry.toRoute<Screen.Detail>()
                    DetailScreen(
                        sourceName = args.sourceName,
                        detailUrl = args.detailUrl,
                        initialTitle = args.title,
                        initialCoverUrl = args.coverUrl,
                        videoRepository = videoRepository,
                        sourceRepository = sourceRepository,
                        userDataRepository = userDataRepository,
                        onNavigateToPlayer = { sourceName, episodeUrl, title ->
                            navController.navigate(Screen.Player(sourceName, episodeUrl, title))
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.Player> { backStackEntry ->
                    val args = backStackEntry.toRoute<Screen.Player>()
                    PlayerScreen(
                        sourceName = args.sourceName,
                        episodeUrl = args.episodeUrl,
                        title = args.title,
                        videoRepository = videoRepository,
                        sourceRepository = sourceRepository,
                        userDataRepository = userDataRepository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.SourceManage> {
                    SourceManageScreen(
                        sourceRepository = sourceRepository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.Settings> {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // 底部导航栏 — 仅在主 Tab 页面显示
        if (isMainTab) {
            AppBottomBar(navController = navController)
        }
    }
}

/**
 * 底部导航栏
 *
 * 使用 Miuix NavigationBar 组件，三个 Tab：首页、搜索、我的。
 * 点击已选中的 Tab 会回到栈顶（popUpTo），避免重复堆叠。
 */
@Composable
private fun AppBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedIndex = remember(currentRoute) {
        BottomTab.entries.indexOfFirst { tab ->
            val name = tab.screen::class.qualifiedName.orEmpty()
            currentRoute?.contains(name) == true
        }.coerceAtLeast(0)
    }

    val items = remember {
        BottomTab.entries.map { tab ->
            NavigationItem(tab.label, tab.label)
        }
    }

    NavigationBar(
        items = items,
        selected = selectedIndex,
        onClick = { index ->
            val targetTab = BottomTab.entries[index]
            navController.navigate(targetTab.screen) {
                popUpTo(Screen.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}
