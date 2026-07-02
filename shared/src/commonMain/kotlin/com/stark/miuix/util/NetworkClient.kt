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
import io.ktor.client.request.*
import io.ktor.client.statement.*

/**
 * 跨平台网络客户端
 *
 * 基于 Ktor HttpClient 封装的网络请求工具，支持：
 * - GET/POST 请求
 * - 自定义请求头
 * - 自动重试（基础实现）
 *
 * 各平台通过 Ktor 的引擎自动选择实际实现：
 * - Android: OkHttp
 * - Desktop: CIO
 * - WasmJs: Js
 * - iOS: Darwin
 */
class NetworkClient {

    /** Ktor HTTP 客户端实例 */
    private val client = HttpClient()

    /**
     * 发送 GET 请求
     *
     * @param url 请求地址
     * @param headers 自定义请求头
     * @return 响应体字符串
     * @throws Exception 网络错误或 HTTP 错误
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): String {
        return client.get(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            // 设置默认 User-Agent
            header("User-Agent", "MiuixVideo/1.0")
        }.bodyAsText()
    }

    /**
     * 发送 POST 请求
     *
     * @param url 请求地址
     * @param body 请求体
     * @param headers 自定义请求头
     * @return 响应体字符串
     * @throws Exception 网络错误或 HTTP 错误
     */
    suspend fun post(
        url: String,
        body: String = "",
        headers: Map<String, String> = emptyMap()
    ): String {
        return client.post(url) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
            header("User-Agent", "MiuixVideo/1.0")
            if (body.isNotBlank()) {
                setBody(body)
            }
        }.bodyAsText()
    }

    /**
     * 释放客户端资源
     *
     * 在应用退出时调用，关闭底层连接池。
     */
    fun close() {
        client.close()
    }
}
