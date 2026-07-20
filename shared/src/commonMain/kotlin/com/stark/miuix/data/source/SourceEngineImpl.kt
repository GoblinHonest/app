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
import com.stark.miuix.data.model.PlayLine
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.data.model.Video
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.parser.RuleParser
import com.stark.miuix.util.LruCache
import com.stark.miuix.util.AppLogger
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
                val rawUrl = ruleParser.parseField(item, source.searchRule.urlRule, source.searchRule.ruleType)
                val resolvedUrl = resolveJsonpathUrl(source.sourceUrl, source.searchRule.urlRule, item, rawUrl)
                SearchResult(
                    title = ruleParser.parseField(item, source.searchRule.titleRule, source.searchRule.ruleType),
                    cover = resolveUrl(source.sourceUrl, ruleParser.parseField(item, source.searchRule.coverRule, source.searchRule.ruleType)),
                    url = resolvedUrl,
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

            // MacCMS API JSON 格式检测：如果 URL 包含 api.php 或 ruleType 为 jsonpath
            if (url.contains("api.php") || rule.ruleType == "jsonpath") {
                return@runCatching parseMacCmsDetail(content, url, source)
            }

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
            // MacCMS API 直链模式：episodeUrl 已经是 m3u8/mp4 直链
            if (episodeUrl.endsWith(".m3u8") || episodeUrl.endsWith(".mp4")
                || episodeUrl.contains(".m3u8?") || episodeUrl.contains(".mp4?")) {
                return@runCatching episodeUrl
            }

            AppLogger.d("Player", "解析播放地址: $episodeUrl")
            AppLogger.d("Player", "规则类型: ${source.playerRule.ruleType}, 规则: ${source.playerRule.playerUrlRule}")

            val content = networkClient.get(
                episodeUrl,
                headers = source.playerRule.playerHeaders
            )
            AppLogger.d("Player", "页面内容长度: ${content.length}")

            var playerUrl = when (source.playerRule.ruleType) {
                "regex" -> {
                    val regex = source.playerRule.playerUrlRule.toRegex()
                    val match = regex.find(content)
                    match?.groupValues?.getOrNull(1)?.replace("\\/", "/") ?: ""
                }
                "js", "json" -> {
                    extractPlayerFromScript(content, source.playerRule.playerUrlRule, episodeUrl)
                }
                "direct" -> {
                    // direct 模式下 URL 不是直链，尝试从页面提取
                    extractPlayerFromScript(content, "$.url", episodeUrl)
                }
                else -> {
                    ruleParser.parseFirst(content, source.playerRule.playerUrlRule, source.playerRule.ruleType)
                }
            }

            // 所有规则类型都尝试 MacCMS 回退
            if (playerUrl.isBlank()) {
                AppLogger.d("Player", "规则解析为空，尝试 MacCMS 回退")
                playerUrl = extractPlayerFromScript(content, "$.url", episodeUrl)
            }

            AppLogger.d("Player", "解析结果: ${playerUrl.take(100)}")
            require(playerUrl.isNotBlank()) { "无法解析播放地址 (ruleType=${source.playerRule.ruleType}, url=$episodeUrl)" }
            playerUrl
        }
    }

    /**
     * 解析 MacCMS API JSON 格式的视频详情
     *
     * MacCMS API 返回格式：
     * {"list":[{"vod_name":"...", "vod_pic":"...", "vod_play_url":"第01集$url1#第02集$url2"}]}
     *
     * vod_play_url 格式：
     * - 多集：第01集$https://xxx.m3u8#第02集$https://yyy.m3u8
     * - 多线路：线路1$playUrl1#线路2$playUrl$$$线路1$playUrl3#线路2$playUrl4
     */
    private fun parseMacCmsDetail(content: String, url: String, source: VideoSource): Video {
        val root = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .parseToJsonElement(content)
        val list = root.jsonObject["list"]?.jsonArray
        val item = list?.firstOrNull()?.jsonObject
            ?: throw Exception("MacCMS API 返回空列表")

        val title = item["vod_name"]?.jsonPrimitive?.content ?: ""
        val cover = item["vod_pic"]?.jsonPrimitive?.content ?: ""
        val description = item["vod_content"]?.jsonPrimitive?.content
            ?: item["vod_blurb"]?.jsonPrimitive?.content ?: ""
        val status = item["vod_remarks"]?.jsonPrimitive?.content ?: ""
        val year = item["vod_year"]?.jsonPrimitive?.content ?: ""
        val area = item["vod_area"]?.jsonPrimitive?.content ?: ""
        val genres = (item["vod_class"]?.jsonPrimitive?.content ?: "")
            .split(Regex("[/,，]")).map { it.trim() }.filter { it.isNotBlank() }

        val playUrlRaw = item["vod_play_url"]?.jsonPrimitive?.content ?: ""
        val playFromRaw = item["vod_play_from"]?.jsonPrimitive?.content ?: ""
        val playLines = parsePlayLines(playUrlRaw, playFromRaw)
        val episodes = playLines.firstOrNull()?.episodes ?: parsePlayUrl(playUrlRaw)

        return Video(
            id = url.hashCode().toString(),
            title = title,
            cover = cover,
            description = description.replace(Regex("<[^>]+>"), ""),
            status = status,
            sourceName = source.sourceName,
            detailUrl = url,
            episodes = episodes,
            playLines = playLines,
            year = year,
            area = area,
            genres = genres
        )
    }

    /**
     * 解析 MacCMS vod_play_url 格式
     *
     * 格式：集名$url#集名$url#...（多线路用 $$$ 分隔，取第一条线路）
     */
    private fun parsePlayUrl(raw: String): List<Episode> {
        if (raw.isBlank()) return emptyList()
        // 取第一条线路（$$$ 分隔多线路）
        val firstLine = raw.split("\\$\\$\\$".toRegex()).first()
        return firstLine.split("#").mapIndexedNotNull { index, segment ->
            val parts = segment.split("$", limit = 2)
            if (parts.size == 2) {
                Episode(
                    name = parts[0].trim(),
                    url = parts[1].trim(),
                    index = index
                )
            } else null
        }
    }

    /**
     * 解析多线路播放地址
     *
     * MacCMS 多线路格式：
     * - vod_play_from: "线路1$$$线路2$$$线路3"
     * - vod_play_url: "集1$url#集2$url$$$集1$url#集2$url$$$集1$url#集2$url"
     *
     * 每个 $$$ 分隔的段对应一条线路。
     */
    private fun parsePlayLines(playUrlRaw: String, playFromRaw: String): List<PlayLine> {
        if (playUrlRaw.isBlank()) return emptyList()

        val urlSegments = playUrlRaw.split("\\$\\$\\$".toRegex())
        val fromSegments = if (playFromRaw.isNotBlank()) {
            playFromRaw.split("\\$\\$\\$".toRegex())
        } else {
            urlSegments.indices.map { "线路${it + 1}" }
        }

        return urlSegments.mapIndexed { index, segment ->
            val episodes = segment.split("#").mapIndexedNotNull { epIndex, epSegment ->
                val parts = epSegment.split("$", limit = 2)
                if (parts.size == 2) {
                    Episode(name = parts[0].trim(), url = parts[1].trim(), index = epIndex)
                } else null
            }
            PlayLine(
                name = fromSegments.getOrNull(index)?.trim() ?: "线路${index + 1}",
                episodes = episodes
            )
        }.filter { it.episodes.isNotEmpty() }
    }

    /**
     * 从页面 script 中提取播放地址
     *
     * 支持多种播放器结构：
     * - player_aaaa={"url":"..."}（MacCMS 标准）
     * - var main = "/path/video.m3u8"（ckplayer/artplayer/DPlayer 常见）
     * - var url = "https://..."
     */
    private fun extractPlayerFromScript(html: String, rule: String, pageUrl: String = ""): String {
        // 如果规则是 jsonpath 格式，先提取 JSON 块再用 jsonpath 解析
        val jsonBlock = """player_aaaa\s*=\s*(\{[^;]+\})""".toRegex().find(html)?.groupValues?.get(1)
        if (jsonBlock != null && rule.isNotBlank()) {
            val result = ruleParser.parseField(jsonBlock, rule, "jsonpath")
            if (result.isNotBlank()) return result.replace("\\/", "/")
        }

        // 兜底：正则匹配常见播放地址模式
        val patterns = listOf(
            """"url"\s*:\s*"([^"]+\.m3u8[^"]*)"""".toRegex(),
            """"url"\s*:\s*"([^"]+\.mp4[^"]*)"""".toRegex(),
            // var/const/let main = "/path/to/video.m3u8?sign=xxx"
            """(?:var|const|let)\s+main\s*=\s*["']([^"']+\.m3u8[^"']*?)["']""".toRegex(),
            // var/const/let main = "https://..."
            """(?:var|const|let)\s+main\s*=\s*["'](https?://[^"']+)["']""".toRegex(),
            // var/const/let url = "/path/to/video.m3u8?sign=xxx"（相对路径）
            """(?:var|const|let)\s+(?:video_?)?url\s*=\s*["'](/[^"']+\.m3u8[^"']*?)["']""".toRegex(),
            // var/const/let url = "https://..."
            """(?:var|const|let)\s+(?:video_?)?url\s*=\s*["'](https?://[^"']+)["']""".toRegex(),
            """"url"\s*:\s*"(https?://[^"]+)"""".toRegex()
        )
        for (pattern in patterns) {
            val match = pattern.find(html)
            if (match != null) {
                val url = match.groupValues[1].replace("\\/", "/")
                // 相对路径：用请求 URL 自身的域名拼接
                if (url.startsWith("/") && pageUrl.isNotBlank()) {
                    val hostMatch = """(https?://[^/\s"']+)""".toRegex().find(pageUrl)
                    if (hostMatch != null) {
                        return hostMatch.groupValues[1] + url
                    }
                }
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
                val rawUrl = ruleParser.parseField(item, rule.urlRule, rule.ruleType)
                SearchResult(
                    title = ruleParser.parseField(item, rule.titleRule, rule.ruleType),
                    cover = resolveUrl(source.sourceUrl, ruleParser.parseField(item, rule.coverRule, rule.ruleType)),
                    url = resolveJsonpathUrl(source.sourceUrl, rule.urlRule, item, rawUrl),
                    sourceName = source.sourceName
                )
            }
        }
    }

    /** 带缓存的页面获取 */
    private suspend fun fetchWithCache(url: String): String {
        return pageCache.getOrPut(url) { networkClient.get(url) }
    }

    /**
     * 解析包含 jsonpath 表达式的 URL 模板
     *
     * 如：/api.php/provide/vod/?ac=detail&ids=$.vod_id
     * 其中 $.vod_id 会被替换为 JSON 中的实际值
     */
    private fun resolveJsonpathUrl(baseUrl: String, urlRule: String, item: String, fallback: String): String {
        if (!urlRule.contains("$.")) {
            return resolveUrl(baseUrl, fallback)
        }
        // 提取模板中的 jsonpath 占位符并替换
        val resolved = urlRule.replace(Regex("\\$\\.([a-zA-Z0-9_]+)")) { match ->
            ruleParser.parseField(item, match.value, "jsonpath")
        }
        if (resolved.startsWith("http://") || resolved.startsWith("https://")) return resolved
        return resolveUrl(baseUrl, resolved)
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
