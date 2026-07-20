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
import com.stark.miuix.data.parser.RuleParser
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 搜索联想服务
 *
 * 从各视频源的联想接口获取搜索建议。
 * MacCMS 标准联想接口：/index.php/ajax/suggest?mid=1&wd={keyword}
 * 返回格式：{"data":[{"name":"庆余年"}]}
 */
class SuggestionService(
    private val networkClient: NetworkClient,
    private val ruleParser: RuleParser
) {
    /**
     * 获取搜索联想词
     *
     * @param sources 启用的源列表
     * @param keyword 用户输入的关键词
     * @param limit 最大返回条数
     * @return 去重后的联想词列表
     */
    suspend fun getSuggestions(
        sources: List<VideoSource>,
        keyword: String,
        limit: Int = 8
    ): List<String> = withContext(Dispatchers.Default) {
        if (keyword.length < 2) return@withContext emptyList()

        val allSuggestions = mutableSetOf<String>()

        for (source in sources.take(3)) {
            try {
                val suggestions = fetchSuggestionsFromSource(source, keyword)
                allSuggestions.addAll(suggestions)
                if (allSuggestions.size >= limit) break
            } catch (_: Exception) {
                // 单源失败不影响其他源
            }
        }

        allSuggestions.take(limit)
    }

    /**
     * 从单个源获取联想词
     *
     * 优先使用源配置的 suggestUrl，回退到 MacCMS 标准接口。
     */
    private suspend fun fetchSuggestionsFromSource(source: VideoSource, keyword: String): List<String> {
        val suggestUrl = buildSuggestUrl(source, keyword)
        if (suggestUrl.isBlank()) return emptyList()

        val content = networkClient.get(suggestUrl)

        return when {
            content.contains("\"data\"") || content.contains("\"result\"") ->
                parseJsonSuggestions(content)
            else ->
                parseHtmlSuggestions(content, source)
        }
    }

    /** 构造联想 URL */
    private fun buildSuggestUrl(source: VideoSource, keyword: String): String {
        val base = source.sourceUrl.trimEnd('/')
        val customUrl = source.searchRule.suggestUrl

        return when {
            customUrl.isNotBlank() -> {
                val url = customUrl.replace("{{keyword}}", keyword)
                if (url.startsWith("http")) url else "$base/${url.trimStart('/')}"
            }
            else -> "$base/index.php/ajax/suggest?mid=1&wd=$keyword&limit=10"
        }
    }

    /** 解析 JSON 格式联想结果 */
    private fun parseJsonSuggestions(content: String): List<String> {
        val regex = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
        return regex.findAll(content).map { it.groupValues[1] }.toList()
    }

    /** 解析 HTML 格式联想结果 */
    private fun parseHtmlSuggestions(content: String, source: VideoSource): List<String> {
        val rule = source.searchRule.suggestRule
        if (rule.isBlank()) return emptyList()
        return ruleParser.selectList(content, rule, source.searchRule.ruleType)
            .map { ruleParser.parseField(it, "@text", source.searchRule.ruleType) }
            .filter { it.isNotBlank() }
    }
}
