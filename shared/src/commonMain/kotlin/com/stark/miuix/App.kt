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
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.data.parser.CompositeRuleParser
import com.stark.miuix.data.parser.HtmlParser
import com.stark.miuix.data.parser.JsonParser
import com.stark.miuix.data.source.SourceEngineImpl
import com.stark.miuix.navigation.AppNavigation
import com.stark.miuix.theme.AppTheme
import com.stark.miuix.util.NetworkClient

/**
 * 应用根 Composable
 *
 * 整体应用架构入口，负责：
 * 1. 注入依赖（SourceEngine、Repository 等）
 * 2. 应用主题包裹（AppTheme → MiuixTheme）
 * 3. 初始化导航控制器
 * 4. 挂载导航图
 *
 * 依赖注入说明：
 * 当前使用简单的手动注入，适合当前规模。
 * 后续可替换为 Koin 或其他 KMP 兼容的 DI 框架。
 */
@Composable
fun App() {
    // 依赖注入
    val networkClient = remember { NetworkClient() }
    val htmlParser = remember { HtmlParser() }
    val jsonParser = remember { JsonParser() }
    val ruleParser = remember { CompositeRuleParser(htmlParser, jsonParser) }
    val sourceEngine = remember { SourceEngineImpl(networkClient, ruleParser) }
    val sourceRepository = remember { SourceRepository() }
    val videoRepository = remember { VideoRepository(sourceEngine, sourceRepository) }

    // 导航控制器
    val navController = rememberNavController()

    // 主题 + 导航
    AppTheme {
        AppNavigation(
            navController = navController,
            videoRepository = videoRepository,
            sourceRepository = sourceRepository
        )
    }
}
