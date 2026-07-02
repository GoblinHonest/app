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

package com.stark.miuix.util

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay

/**
 * 跨平台网络客户端
 *
 * 基于 Ktor HttpClient 封装，提供：
 * - GET/POST 请求
 * - 自动重试（指数退避，最多 3 次）
 * - 浏览器 User-Agent 伪装
 * - 超时保护（连接 15s / 请求 20s / Socket 15s）
 *
 * 各平台引擎自动选择：Android=OkHttp, Desktop=CIO, Web=Js, iOS=Darwin
 */
class NetworkClient {

    private val client = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 20_000
            socketTimeoutMillis = 15_000
        }
    }

    /**
     * 发送 GET 请求（带自动重试）
     *
     * @param url 请求地址
     * @param headers 自定义请求头
     * @param maxRetries 最大重试次数
     * @return 响应体字符串
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        maxRetries: Int = MAX_RETRIES
    ): String {
        return withRetry(maxRetries) {
            client.get(url) {
                applyHeaders(headers)
            }.bodyAsText()
        }
    }

    /**
     * 发送 POST 请求（带自动重试）
     *
     * @param url 请求地址
     * @param body 请求体
     * @param headers 自定义请求头
     * @return 响应体字符串
     */
    suspend fun post(
        url: String,
        body: String = "",
        headers: Map<String, String> = emptyMap()
    ): String {
        return withRetry(MAX_RETRIES) {
            client.post(url) {
                applyHeaders(headers)
                if (body.isNotBlank()) {
                    setBody(body)
                }
            }.bodyAsText()
        }
    }

    fun close() {
        client.close()
    }

    private fun HttpRequestBuilder.applyHeaders(custom: Map<String, String>) {
        header("User-Agent", USER_AGENT)
        header("Accept", "text/html,application/json,*/*")
        header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
        custom.forEach { (key, value) -> header(key, value) }
    }

    /**
     * 指数退避重试
     *
     * 首次失败等 500ms，第二次 1s，第三次 2s。
     * 超过最大次数后抛出最后一次异常。
     */
    private suspend fun <T> withRetry(maxRetries: Int, block: suspend () -> T): T {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(INITIAL_BACKOFF_MS * (1L shl attempt))
                }
            }
        }
        throw lastException ?: Exception("请求失败")
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 500L
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}
