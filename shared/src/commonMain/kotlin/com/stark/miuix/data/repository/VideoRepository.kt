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

package com.stark.miuix.data.repository

import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.Video
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.source.SourceEngine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 视频仓库 — 业务层核心协调器
 *
 * 职责：
 * - 聚合搜索：并行查询所有已启用视频源，合并结果
 * - 详情获取：代理 SourceEngine 的详情解析
 * - 播放地址：代理 SourceEngine 的播放 URL 提取
 * - 搜索历史：维护最近 20 条搜索关键词
 *
 * 设计原则：上层 ViewModel 只依赖本仓库，不直接操作 SourceEngine。
 */
class VideoRepository(
    private val sourceEngine: SourceEngine,
    private val sourceRepository: SourceRepository
) {
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())

    /** 搜索历史（最新在前，上限 20 条） */
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    /**
     * 聚合搜索
     *
     * 并行查询所有已启用视频源，将各源结果合并返回。
     * 单个源失败不影响其他源的结果（静默降级为空列表）。
     */
    suspend fun search(keyword: String): Result<List<SearchResult>> = coroutineScope {
        runCatching {
            addToHistory(keyword)
            val enabledSources = sourceRepository.getEnabledSources()
            val results = enabledSources.map { source ->
                async { sourceEngine.search(source, keyword).getOrDefault(emptyList()) }
            }.awaitAll()
            results.flatten()
        }
    }

    /** 获取视频详情（标题、简介、剧集列表） */
    suspend fun getDetail(source: VideoSource, url: String): Result<Video> {
        return sourceEngine.getDetail(source, url)
    }

    /** 解析实际播放地址 */
    suspend fun getPlayerUrl(source: VideoSource, episodeUrl: String): Result<String> {
        return sourceEngine.getPlayerUrl(source, episodeUrl)
    }

    /** 获取分类页视频列表（支持分页） */
    suspend fun getCategoryVideos(
        source: VideoSource,
        categoryUrl: String = "",
        page: Int = 1
    ): Result<List<SearchResult>> {
        return sourceEngine.getCategoryVideos(source, categoryUrl, page)
    }

    /** 清空搜索历史 */
    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

    /** 将关键词追加到历史队首（去重 + 限长） */
    private fun addToHistory(keyword: String) {
        val current = _searchHistory.value.toMutableList()
        current.remove(keyword)
        current.add(0, keyword)
        if (current.size > MAX_HISTORY_SIZE) {
            current.removeAt(current.lastIndex)
        }
        _searchHistory.value = current
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 20
    }
}
