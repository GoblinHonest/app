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

package com.stark.miuix.data.source

import com.stark.miuix.data.model.Episode
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.Video
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.parser.RuleParser
import com.stark.miuix.util.NetworkClient

/**
 * 视频源引擎实现
 *
 * 通过规则解析器和网络客户端实现视频源的搜索、详情获取和播放地址解析。
 * 根据视频源中定义的规则类型（css/xpath/jsonpath）选择对应的解析策略。
 *
 * @property networkClient 网络请求客户端
 * @property ruleParser 规则解析器
 */
class SourceEngineImpl(
    private val networkClient: NetworkClient,
    private val ruleParser: RuleParser
) : SourceEngine {

    override suspend fun search(source: VideoSource, keyword: String): Result<List<SearchResult>> {
        return runCatching {
            val searchUrl = source.searchRule.searchUrl.replace("{{keyword}}", keyword)
            val fullUrl = resolveUrl(source.sourceUrl, searchUrl)
            val content = networkClient.get(fullUrl)

            val items = ruleParser.selectList(
                content,
                source.searchRule.listRule,
                source.searchRule.ruleType
            )

            items.map { item ->
                SearchResult(
                    title = ruleParser.parseField(item, source.searchRule.titleRule, source.searchRule.ruleType),
                    cover = resolveUrl(source.sourceUrl, ruleParser.parseField(item, source.searchRule.coverRule, source.searchRule.ruleType)),
                    url = resolveUrl(source.sourceUrl, ruleParser.parseField(item, source.searchRule.urlRule, source.searchRule.ruleType)),
                    description = ruleParser.parseField(item, source.searchRule.descRule, source.searchRule.ruleType),
                    sourceName = source.sourceName
                )
            }
        }
    }

    override suspend fun getDetail(source: VideoSource, url: String): Result<Video> {
        return runCatching {
            val content = networkClient.get(url)
            val rule = source.detailRule

            val episodeElements = ruleParser.selectList(
                content,
                rule.episodeListRule,
                rule.ruleType
            )

            val episodes = episodeElements.mapIndexed { index, element ->
                Episode(
                    name = ruleParser.parseField(element, rule.episodeNameRule, rule.ruleType),
                    url = resolveUrl(source.sourceUrl, ruleParser.parseField(element, rule.episodeUrlRule, rule.ruleType)),
                    index = index
                )
            }

            Video(
                id = url.hashCode().toString(),
                title = ruleParser.parseFirst(content, rule.titleRule, rule.ruleType),
                cover = resolveUrl(source.sourceUrl, ruleParser.parseFirst(content, rule.coverRule, rule.ruleType)),
                description = ruleParser.parseFirst(content, rule.descRule, rule.ruleType),
                status = ruleParser.parseFirst(content, rule.statusRule, rule.ruleType),
                sourceName = source.sourceName,
                detailUrl = url,
                episodes = episodes
            )
        }
    }

    override suspend fun getPlayerUrl(source: VideoSource, episodeUrl: String): Result<String> {
        return runCatching {
            val content = networkClient.get(episodeUrl)
            val playerUrl = ruleParser.parseFirst(
                content,
                source.playerRule.playerUrlRule,
                source.playerRule.ruleType
            )
            require(playerUrl.isNotBlank()) { "无法解析播放地址" }
            playerUrl
        }
    }

    override suspend fun getCategoryVideos(
        source: VideoSource,
        categoryUrl: String,
        page: Int
    ): Result<List<SearchResult>> {
        return runCatching {
            val url = categoryUrl.ifBlank { source.categoryRule.categoryUrl }
            val fullUrl = resolveUrl(source.sourceUrl, url)
                .replace("{{page}}", page.toString())
            val content = networkClient.get(fullUrl)
            val rule = source.categoryRule

            val items = ruleParser.selectList(content, rule.videoListRule, rule.ruleType)

            items.map { item ->
                SearchResult(
                    title = ruleParser.parseField(item, rule.titleRule, rule.ruleType),
                    cover = resolveUrl(source.sourceUrl, ruleParser.parseField(item, rule.coverRule, rule.ruleType)),
                    url = resolveUrl(source.sourceUrl, ruleParser.parseField(item, rule.urlRule, rule.ruleType)),
                    sourceName = source.sourceName
                )
            }
        }
    }

    /**
     * 解析相对 URL 为绝对 URL
     */
    private fun resolveUrl(baseUrl: String, path: String): String {
        if (path.isBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = baseUrl.trimEnd('/')
        val relativePath = path.trimStart('/')
        return "$base/$relativePath"
    }
}
