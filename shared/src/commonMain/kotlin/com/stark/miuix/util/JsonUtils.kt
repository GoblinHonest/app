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
 * 内部使用 kotlinx.serialization，确保跨平台兼容性。
 */
object JsonUtils {

    /**
     * 配置 JSON 实例
     *
     * - ignoreUnknownKeys: 忽略未知字段，向前兼容新版本视频源格式
     * - prettyPrint: 格式化输出，便于人类阅读和调试
     * - isLenient: 宽松模式，容忍非标准 JSON
     */
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    /**
     * 将 JSON 字符串解码为视频源列表
     *
     * 支持两种格式：
     * - JSON Array: 批量导入多个视频源
     * - JSON Object: 单个视频源
     *
     * @param jsonString JSON 字符串
     * @return 解析后的视频源列表
     * @throws kotlinx.serialization.SerializationException JSON 格式错误
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
     *
     * @param sources 视频源列表
     * @return 格式化的 JSON 字符串
     */
    fun encodeVideoSources(sources: List<VideoSource>): String {
        return json.encodeToString(sources)
    }

    /**
     * 将单个视频源编码为 JSON 字符串
     *
     * @param source 视频源
     * @return 格式化的 JSON 字符串
     */
    fun encodeVideoSource(source: VideoSource): String {
        return json.encodeToString(source)
    }

    /**
     * 通用 JSON 解码
     *
     * @param jsonString JSON 字符串
     * @return 解析后的对象
     */
    inline fun <reified T> decode(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }

    /**
     * 通用 JSON 编码
     *
     * @param obj 要序列化的对象
     * @return JSON 字符串
     */
    inline fun <reified T> encode(obj: T): String {
        return json.encodeToString(obj)
    }
}
