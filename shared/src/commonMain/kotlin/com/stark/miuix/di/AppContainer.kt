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
import com.stark.miuix.data.source.SourceHealthChecker
import com.stark.miuix.data.source.SourceRepoManager
import com.stark.miuix.data.source.SuggestionService
import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.data.dlna.DlnaController
import com.stark.miuix.data.storage.getAppDataDir
import com.stark.miuix.data.subtitle.SubtitleParser
import com.stark.miuix.data.download.DownloadManager
import com.stark.miuix.data.danmaku.DanmakuService
import com.stark.miuix.data.recommend.RecommendEngine
import com.stark.miuix.data.sync.SyncManager
import com.stark.miuix.ui.search.SearchViewModel
import com.stark.miuix.util.NetworkClient

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
    val dlnaController by lazy { DlnaController(networkClient) }
    val healthChecker by lazy { SourceHealthChecker(networkClient) }
    val suggestionService by lazy { SuggestionService(networkClient, ruleParser) }
    val subtitleParser by lazy { SubtitleParser() }
    val downloadManager by lazy { DownloadManager(networkClient, localStorage) }
    val recommendEngine by lazy { RecommendEngine(userDataRepository, videoRepository) }
    val syncManager by lazy { SyncManager(localStorage, networkClient) }
    val danmakuService by lazy { DanmakuService(networkClient) }
    val sourceRepoManager by lazy { SourceRepoManager(networkClient, sourceRepository) }

    val searchViewModel by lazy { SearchViewModel(videoRepository, suggestionService, sourceRepository) }
}
