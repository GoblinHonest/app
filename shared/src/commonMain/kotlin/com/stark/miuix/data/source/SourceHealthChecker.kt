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
import com.stark.miuix.util.AppLogger
import com.stark.miuix.util.NetworkClient
import com.stark.miuix.util.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 源健康检测结果
 */
data class SourceCheckResult(
    val sourceName: String,
    val status: CheckStatus,
    val latencyMs: Long = 0,
    val message: String = ""
)

/** 检测状态 */
enum class CheckStatus {
    OK,
    TIMEOUT,
    PARSE_ERROR,
    UNREACHABLE
}

/**
 * 视频源健康检测器
 *
 * 对每个启用源发起一次分类请求（取第一页），判断：
 * - HTTP 是否可达
 * - 响应是否包含预期结构
 * - 响应时间
 *
 * 所有源并行检测，总超时 30 秒。
 */
class SourceHealthChecker(
    private val networkClient: NetworkClient
) {
    /**
     * 检测单个源的健康状态
     */
    suspend fun checkSource(source: VideoSource): SourceCheckResult = withContext(Dispatchers.Default) {
        val startTime = currentTimeMillis()
        try {
            val checkUrl = buildCheckUrl(source)
            val content = withTimeoutOrNull(CHECK_TIMEOUT_MS) {
                networkClient.get(checkUrl)
            }

            if (content == null) {
                return@withContext SourceCheckResult(source.sourceName, CheckStatus.TIMEOUT, message = "请求超时")
            }

            val latency = currentTimeMillis() - startTime

            if (content.isBlank() || content.length < 10) {
                return@withContext SourceCheckResult(source.sourceName, CheckStatus.PARSE_ERROR, latency, "响应内容为空")
            }

            val isValid = when {
                source.categoryRule.ruleType == "jsonpath" || checkUrl.contains("api.php") ->
                    content.contains("\"list\"") || content.contains("\"code\"")
                else ->
                    content.contains("<") || content.contains("{")
            }

            if (isValid) {
                SourceCheckResult(source.sourceName, CheckStatus.OK, latency)
            } else {
                SourceCheckResult(source.sourceName, CheckStatus.PARSE_ERROR, latency, "响应格式异常")
            }
        } catch (e: Exception) {
            val latency = currentTimeMillis() - startTime
            AppLogger.e("HealthCheck", "源[${source.sourceName}]检测失败", e)
            SourceCheckResult(source.sourceName, CheckStatus.UNREACHABLE, latency, e.message ?: "连接失败")
        }
    }

    /**
     * 并行检测所有源
     *
     * @param sources 待检测的源列表
     * @param onProgress 进度回调 (已完成数, 总数)
     * @return 所有检测结果
     */
    suspend fun checkAll(
        sources: List<VideoSource>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<SourceCheckResult> = coroutineScope {
        var completed = 0
        sources.map { source ->
            async {
                val result = checkSource(source)
                completed++
                onProgress(completed, sources.size)
                result
            }
        }.awaitAll()
    }

    /** 构造检测 URL：优先用分类 URL，回退到源首页 */
    private fun buildCheckUrl(source: VideoSource): String {
        val categoryUrl = source.categoryRule.categoryUrl
        val base = source.sourceUrl.trimEnd('/')
        return when {
            categoryUrl.isNotBlank() -> {
                if (categoryUrl.startsWith("http")) categoryUrl
                else "$base/${categoryUrl.trimStart('/')}"
            }
            else -> base
        }
    }

    companion object {
        private const val CHECK_TIMEOUT_MS = 10_000L
    }
}
