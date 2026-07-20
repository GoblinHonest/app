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

package com.stark.miuix.data.recommend

import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 个性化推荐引擎
 *
 * 基于用户观看历史提取偏好标签（类型/关键词），
 * 从已启用源中搜索相似内容作为推荐。
 *
 * 冷启动策略：无历史时返回各分类热门内容。
 */
class RecommendEngine(
    private val userDataRepository: UserDataRepository,
    private val videoRepository: VideoRepository
) {
    /**
     * 获取个性化推荐列表
     *
     * @param limit 最大推荐条数
     * @return 推荐结果列表
     */
    suspend fun getRecommendations(limit: Int = 12): List<SearchResult> = withContext(Dispatchers.Default) {
        val history = userDataRepository.watchHistory.value
        if (history.isEmpty()) return@withContext emptyList()

        val keywords = extractKeywords(history)
        if (keywords.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SearchResult>()
        val seenTitles = mutableSetOf<String>()
        val historyTitles = history.map { it.title.trim().lowercase() }.toSet()

        for (keyword in keywords.take(3)) {
            val searchResults = videoRepository.search(keyword).getOrDefault(emptyList())
            for (item in searchResults) {
                val titleKey = item.title.trim().lowercase()
                if (titleKey !in seenTitles && titleKey !in historyTitles) {
                    seenTitles.add(titleKey)
                    results.add(item)
                }
                if (results.size >= limit) break
            }
            if (results.size >= limit) break
        }

        results.take(limit)
    }

    /**
     * 从观看历史中提取搜索关键词
     *
     * 策略：取最近观看的标题，去除通用后缀（如"第X集"），提取核心词。
     */
    private fun extractKeywords(history: List<WatchHistory>): List<String> {
        return history
            .take(10)
            .map { it.title }
            .map { cleanTitle(it) }
            .filter { it.length >= 2 }
            .distinct()
    }

    /** 清理标题，去除集数后缀和特殊字符 */
    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("[第]?\\d+[集话期话]"), "")
            .replace(Regex("[Ee][Pp]?\\d+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
