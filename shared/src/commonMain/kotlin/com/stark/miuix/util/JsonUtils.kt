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

import com.stark.miuix.data.model.VideoSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * JSON 工具类
 *
 * 提供 JSON 序列化/反序列化功能，用于视频源的导入导出。
 */
object JsonUtils {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    /**
     * 将 JSON 字符串解码为视频源列表
     */
    fun decodeVideoSources(jsonString: String): List<VideoSource> {
        val trimmed = jsonString.trim()
        return if (trimmed.startsWith("[")) {
            json.decodeFromString<List<VideoSource>>(trimmed)
        } else {
            listOf(json.decodeFromString<VideoSource>(trimmed))
        }
    }

    /**
     * 将视频源列表编码为 JSON 字符串
     */
    fun encodeVideoSources(sources: List<VideoSource>): String {
        return json.encodeToString(sources)
    }

    /**
     * 将单个视频源编码为 JSON 字符串
     */
    fun encodeVideoSource(source: VideoSource): String {
        return json.encodeToString(source)
    }
}