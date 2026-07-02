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

package com.stark.miuix.di

import com.stark.miuix.data.parser.CompositeRuleParser
import com.stark.miuix.data.parser.HtmlParser
import com.stark.miuix.data.parser.JsonParser
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import com.stark.miuix.data.source.SourceEngineImpl
import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.data.storage.getAppDataDir
import com.stark.miuix.ui.search.SearchViewModel
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 全局服务定位器
 *
 * 应用级依赖容器，所有单例通过 `lazy` 延迟初始化。
 * 持有 [LocalStorage] 用于数据持久化，
 * [appScope] 用于不绑定 UI 生命周期的后台任务。
 */
object AppContainer {

    val networkClient by lazy { NetworkClient() }
    val localStorage by lazy { LocalStorage(getAppDataDir()) }

    private val htmlParser by lazy { HtmlParser() }
    private val jsonParser by lazy { JsonParser() }
    private val ruleParser by lazy { CompositeRuleParser(htmlParser, jsonParser) }

    val sourceEngine by lazy { SourceEngineImpl(networkClient, ruleParser) }
    val sourceRepository by lazy { SourceRepository(localStorage) }
    val userDataRepository by lazy { UserDataRepository(localStorage) }
    val videoRepository by lazy { VideoRepository(sourceEngine, sourceRepository) }

    private val appScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    val searchViewModel by lazy { SearchViewModel(videoRepository, appScope) }
}
