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

package com.stark.miuix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.stark.miuix.di.AppContainer
import com.stark.miuix.navigation.AppNavigation
import com.stark.miuix.theme.AppTheme
import miuix_app.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * 应用根 Composable
 *
 * 职责精简为三步：加载内置资源 → 包裹主题 → 挂载导航。
 * 所有业务依赖由 [AppContainer] 单例管理，不再依赖 remember。
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    LaunchedEffect(Unit) {
        try {
            val bytes = Res.readBytes("files/example_source.json")
            AppContainer.sourceRepository.initWithDefaultsIfEmpty(bytes.decodeToString())
        } catch (_: Exception) { }
    }

    val navController = rememberNavController()

    AppTheme {
        AppNavigation(
            navController = navController,
            videoRepository = AppContainer.videoRepository,
            sourceRepository = AppContainer.sourceRepository,
            userDataRepository = AppContainer.userDataRepository
        )
    }
}
