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

import com.stark.miuix.data.model.Episode
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.Video
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.parser.RuleParser
import com.stark.miuix.util.LruCache
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 视频源引擎实现
 *
 * 通过规则解析器和网络客户端实现视频源的搜索、详情获取和播放地址解析。
 * 内置 LRU 缓存层：网页内容缓存 5 分钟，避免重复请求。
 * 所有网络 I/O 和解析操作在 [Dispatchers.Default] 上执行，不阻塞主线程。
 */
class SourceEngineImpl(
    private val networkClient: NetworkClient,
    private val ruleParser: RuleParser
) : SourceEngine {

    private val pageCache = LruCache<String>(maxSize = 200, ttlMillis = 5 * 60 * 1000L)

    override suspend fun search(
        source: VideoSource,
        keyword: String
    ): Result<List<SearchResult>> = withContext(Dispatchers.Default) {
        runCatching {
            val searchUrl = source.searchRule.searchUrl.replace("{{keyword}}", keyword)
            val fullUrl = resolveUrl(source.sourceUrl, searchUrl)
            val content = fetchWithCache(fullUrl)

            val items = ruleParser.selectList(
                content, source.searchRule.listRule, source.searchRule.ruleType
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

    override suspend fun getDetail(
        source: VideoSource,
        url: String
    ): Result<Video> = withContext(Dispatchers.Default) {
        runCatching {
            val content = fetchWithCache(url)
            val rule = source.detailRule

            val episodeElements = ruleParser.selectList(
                content, rule.episodeListRule, rule.ruleType
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

    override suspend fun getPlayerUrl(
        source: VideoSource,
        episodeUrl: String
    ): Result<String> = withContext(Dispatchers.Default) {
        runCatching {
            val content = networkClient.get(
                episodeUrl,
                headers = source.playerRule.playerHeaders
            )

            // 优先尝试规则解析
            var playerUrl = ruleParser.parseFirst(
                content, source.playerRule.playerUrlRule, source.playerRule.ruleType
            )

            // 回退：从 MacCMS player_aaaa JSON 中提取播放地址
            if (playerUrl.isBlank()) {
                playerUrl = extractPlayerFromScript(content)
            }

            require(playerUrl.isNotBlank()) { "无法解析播放地址" }
            playerUrl
        }
    }

    /**
     * 从 MacCMS 播放页的 player_aaaa JavaScript 变量中提取视频 URL
     *
     * MacCMS 站点将播放信息存储在页面 script 中：
     * `player_aaaa={"url":"https://...m3u8",...}`
     */
    private fun extractPlayerFromScript(html: String): String {
        val patterns = listOf(
            """"url"\s*:\s*"([^"]+\.m3u8[^"]*)"""".toRegex(),
            """"url"\s*:\s*"([^"]+\.mp4[^"]*)"""".toRegex(),
            """"url"\s*:\s*"(https?://[^"]+)"""".toRegex()
        )
        for (pattern in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                val url = match.groupValues[1].replace("\\/", "/")
                if (url.startsWith("http")) return url
            }
        }
        return ""
    }

    override suspend fun getCategoryVideos(
        source: VideoSource,
        categoryUrl: String,
        page: Int
    ): Result<List<SearchResult>> = withContext(Dispatchers.Default) {
        runCatching {
            val url = categoryUrl.ifBlank { source.categoryRule.categoryUrl }
            val fullUrl = resolveUrl(source.sourceUrl, url)
                .replace("{{page}}", page.toString())
            val content = fetchWithCache(fullUrl)
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

    /** 带缓存的页面获取 */
    private suspend fun fetchWithCache(url: String): String {
        return pageCache.getOrPut(url) { networkClient.get(url) }
    }

    /** 解析相对 URL 为绝对 URL */
    private fun resolveUrl(baseUrl: String, path: String): String {
        if (path.isBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = baseUrl.trimEnd('/')
        val relativePath = path.trimStart('/')
        return "$base/$relativePath"
    }
}
