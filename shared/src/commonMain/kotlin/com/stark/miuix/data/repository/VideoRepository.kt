/*
 * Copyright 2024 Starter
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
 * 视频仓库
 *
 * 提供视频搜索、详情获取等操作，内部协调多个视频源引擎并行工作。
 * 搜索时会同时查询所有已启用的视频源，合并结果后返回。
 *
 * @property sourceEngine 视频源引擎
 * @property sourceRepository 视频源仓库（用于获取已启用源列表）
 */
class VideoRepository(
    private val sourceEngine: SourceEngine,
    private val sourceRepository: SourceRepository
) {

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())

    /** 搜索历史记录 */
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    /**
     * 搜索视频
     *
     * 并行查询所有已启用的视频源，合并去重后返回结果。
     *
     * @param keyword 搜索关键词
     * @return 合并后的搜索结果列表
     */
    suspend fun search(keyword: String): Result<List<SearchResult>> = coroutineScope {
        runCatching {
            addToHistory(keyword)
            val enabledSources = sourceRepository.getEnabledSources()
            val results = enabledSources.map { source ->
                async {
                    sourceEngine.search(source, keyword).getOrDefault(emptyList())
                }
            }.awaitAll()
            results.flatten()
        }
    }

    /**
     * 获取视频详情
     *
     * @param source 视频源
     * @param url 详情页 URL
     * @return 视频详情信息
     */
    suspend fun getDetail(source: VideoSource, url: String): Result<Video> {
        return sourceEngine.getDetail(source, url)
    }

    /**
     * 获取播放地址
     *
     * @param source 视频源
     * @param episodeUrl 剧集 URL
     * @return 可播放的视频 URL
     */
    suspend fun getPlayerUrl(source: VideoSource, episodeUrl: String): Result<String> {
        return sourceEngine.getPlayerUrl(source, episodeUrl)
    }

    /**
     * 获取分类视频列表
     *
     * @param source 视频源
     * @param categoryUrl 分类 URL
     * @param page 页码
     * @return 搜索结果列表
     */
    suspend fun getCategoryVideos(
        source: VideoSource,
        categoryUrl: String = "",
        page: Int = 1
    ): Result<List<SearchResult>> {
        return sourceEngine.getCategoryVideos(source, categoryUrl, page)
    }

    /**
     * 清除搜索历史
     */
    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

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
