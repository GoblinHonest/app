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

package com.stark.miuix.data.source

import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.util.AppLogger
import com.stark.miuix.util.JsonUtils
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * 在线源仓库条目
 */
data class SourceRepoItem(
    val name: String,
    val url: String,
    val description: String = "",
    val version: Int = 1,
    val subscribed: Boolean = false
)

/**
 * 在线源仓库管理器
 *
 * 支持从远程仓库拉取源列表、一键导入、订阅自动更新。
 * 内置默认仓库地址，用户可添加自定义仓库。
 */
class SourceRepoManager(
    private val networkClient: NetworkClient,
    private val sourceRepository: SourceRepository
) {
    private val _repos = MutableStateFlow<List<SourceRepoItem>>(DEFAULT_REPOS)
    val repos: StateFlow<List<SourceRepoItem>> = _repos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    /** 从仓库 URL 拉取源列表并导入 */
    suspend fun importFromRepo(repoUrl: String): Result<Int> = withContext(Dispatchers.Default) {
        runCatching {
            _isLoading.value = true
            val json = networkClient.get(repoUrl)
            val sources = JsonUtils.decodeVideoSources(json)
            sources.forEach { sourceRepository.addSource(it) }
            _message.value = "成功导入 ${sources.size} 个源"
            sources.size
        }.onFailure { e ->
            _message.value = "导入失败: ${e.message}"
        }.also {
            _isLoading.value = false
        }
    }

    /** 添加自定义仓库 */
    fun addRepo(name: String, url: String) {
        _repos.value = _repos.value + SourceRepoItem(name = name, url = url)
    }

    /** 切换订阅状态 */
    fun toggleSubscribe(repoUrl: String) {
        _repos.value = _repos.value.map {
            if (it.url == repoUrl) it.copy(subscribed = !it.subscribed) else it
        }
    }

    /** 刷新所有已订阅仓库 */
    suspend fun refreshSubscribed() {
        val subscribed = _repos.value.filter { it.subscribed }
        subscribed.forEach { repo ->
            importFromRepo(repo.url)
        }
    }

    fun clearMessage() {
        _message.value = ""
    }

    companion object {
        private val DEFAULT_REPOS = listOf(
            SourceRepoItem(
                name = "CineHub 官方源",
                url = "https://raw.githubusercontent.com/example/cinehub-sources/main/sources.json",
                description = "官方维护的高质量视频源集合"
            ),
            SourceRepoItem(
                name = "社区精选源",
                url = "https://raw.githubusercontent.com/example/community-sources/main/sources.json",
                description = "社区贡献的热门视频源"
            )
        )
    }
}
