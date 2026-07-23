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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
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
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.UrlEncoder
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
    ) {
        // 内容区底部 padding = 底栏图标高度 + 系统导航栏高度，与悬浮底栏实际高度完全一致
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (isMainTab) DesignTokens.bottomBarHeight + navBarBottom else 0.dp)
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
                composable<Screen.Home> {
                    HomeScreen(
                        videoRepository = videoRepository,
                        sourceRepository = sourceRepository,
                        userDataRepository = userDataRepository,
                        onNavigateToSearch = { navController.navigate(Screen.Search) },
                        onNavigateToCategory = { sourceName, categoryUrl ->
                            navController.navigate(Screen.Category(sourceName, UrlEncoder.encode(categoryUrl)))
                        },
                        onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                            navController.navigate(Screen.Detail(sourceName, UrlEncoder.encode(detailUrl), title, UrlEncoder.encode(coverUrl)))
                        },
                        onNavigateToSourceManage = { navController.navigate(Screen.SourceManage) }
                    )
                }

                composable<Screen.Search> {
                    SearchScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                            navController.navigate(Screen.Detail(sourceName, UrlEncoder.encode(detailUrl), title, UrlEncoder.encode(coverUrl)))
                        }
                    )
                }

                composable<Screen.Profile> {
                    ProfileScreen(
                        userDataRepository = userDataRepository,
                        onNavigateToSourceManage = { navController.navigate(Screen.SourceManage) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings) },
                        onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                            navController.navigate(Screen.Detail(sourceName, UrlEncoder.encode(detailUrl), title, UrlEncoder.encode(coverUrl)))
                        },
                        onNavigateToDownloads = { navController.navigate(Screen.Downloads) },
                        onNavigateToSourceRepo = { navController.navigate(Screen.SourceRepo) },
                        onNavigateToSearch = { navController.navigate(Screen.Search) },
                        onNavigateToHistory = { navController.navigate(Screen.History) }
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
                            navController.navigate(Screen.Detail(sourceName, UrlEncoder.encode(detailUrl), title, UrlEncoder.encode(coverUrl)))
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
                        onNavigateToPlayer = { sourceName, episodeUrl, title, startPosition ->
                            navController.navigate(Screen.Player(sourceName, UrlEncoder.encode(episodeUrl), title, startPosition))
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
                        startPosition = args.startPosition,
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

                composable<Screen.Downloads> {
                    com.stark.miuix.ui.download.DownloadManageScreen(
                        downloadManager = com.stark.miuix.di.AppContainer.downloadManager,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.SourceRepo> {
                    com.stark.miuix.ui.source.SourceRepoScreen(
                        sourceRepoManager = com.stark.miuix.di.AppContainer.sourceRepoManager,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Screen.History> {
                    com.stark.miuix.ui.history.HistoryScreen(
                        userDataRepository = userDataRepository,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetail = { sourceName, detailUrl, title, coverUrl ->
                            navController.navigate(
                                Screen.Detail(
                                    sourceName,
                                    UrlEncoder.encode(detailUrl),
                                    title,
                                    UrlEncoder.encode(coverUrl)
                                )
                            )
                        }
                    )
                }
            }
        }

        // 底部导航栏 — 悬浮玻璃效果
        if (isMainTab) {
            AppBottomBar(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * 底部导航栏 — 悬浮玻璃效果
 *
 * 半透明背景 + 叠加在内容上方，iOS 风格毛玻璃质感。
 * Tab 选中态使用动画颜色过渡。
 */
@Composable
private fun AppBottomBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedIndex = remember(currentRoute) {
        BottomTab.entries.indexOfFirst { tab ->
            val name = tab.screen::class.qualifiedName.orEmpty()
            currentRoute?.contains(name) == true
        }.coerceAtLeast(0)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface.copy(alpha = 0.85f))
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.08f))
                .align(Alignment.TopCenter)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.bottomBarHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val targetColor = if (selected) AppColors.brand()
                                  else MiuixTheme.colorScheme.outline
                val animatedColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(200)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            navController.navigate(tab.screen) {
                                popUpTo(Screen.Home) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = DesignTokens.spacingSm)
                ) {
                    // 自绘 SVG 路径图标（逆向: home.svg / search.svg / user.svg）
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(tab.imageVector),
                        contentDescription = tab.label,
                        colorFilter = ColorFilter.tint(animatedColor),
                        modifier = Modifier
                            .size(22.dp)
                            .padding(bottom = 2.dp)
                    )
                    Text(
                        text = tab.label,
                        style = MiuixTheme.textStyles.footnote2,
                        color = animatedColor
                    )
                }
            }
        }
    }
}
