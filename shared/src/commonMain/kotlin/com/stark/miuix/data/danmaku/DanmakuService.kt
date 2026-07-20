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

package com.stark.miuix.data.danmaku

import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 弹幕条目
 *
 * @property timeMs 出现时间（毫秒）
 * @property text 弹幕文本
 * @property color 颜色（ARGB 整数）
 * @property mode 模式：1=滚动 4=底部 5=顶部
 * @property fontSize 字号：25=小 18=中 36=大
 */
data class DanmakuEntry(
    val timeMs: Long,
    val text: String,
    val color: Int = 0xFFFFFF,
    val mode: Int = 1,
    val fontSize: Int = 25
)

/**
 * 弹幕数据服务
 *
 * 从弹弹play开放API / Bilibili弹幕接口获取弹幕数据。
 * 按视频标题+集数匹配弹幕。
 */
class DanmakuService(
    private val networkClient: NetworkClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 获取弹幕列表
     *
     * @param title 视频标题
     * @param episodeIndex 集数索引（0-based）
     * @return 按时间排序的弹幕列表
     */
    suspend fun fetchDanmaku(title: String, episodeIndex: Int): List<DanmakuEntry> = withContext(Dispatchers.Default) {
        runCatching {
            val cleanTitle = title.replace(Regex("[第]?\\d+[集话期]"), "").trim()
            val url = "https://api.dandanplay.net/api/v2/comment/${cleanTitle}/${episodeIndex + 1}"
            val content = networkClient.get(url)
            parseDandanplayResponse(content)
        }.getOrDefault(emptyList())
    }

    /** 解析弹弹play API 响应 */
    private fun parseDandanplayResponse(content: String): List<DanmakuEntry> {
        return runCatching {
            val root = json.parseToJsonElement(content).jsonObject
            val comments = root["comments"]?.jsonArray ?: return emptyList()
            comments.mapNotNull { element ->
                val obj = element.jsonObject
                val p = obj["p"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val parts = p.split(",")
                if (parts.size < 4) return@mapNotNull null
                val timeSec = parts[0].toDoubleOrNull() ?: return@mapNotNull null
                val mode = parts[1].toIntOrNull() ?: 1
                val fontSize = parts[2].toIntOrNull() ?: 25
                val color = parts[3].toLongOrNull()?.toInt() ?: 0xFFFFFF
                val text = obj["m"]?.jsonPrimitive?.content ?: return@mapNotNull null
                DanmakuEntry(
                    timeMs = (timeSec * 1000).toLong(),
                    text = text,
                    color = color,
                    mode = mode,
                    fontSize = fontSize
                )
            }.sortedBy { it.timeMs }
        }.getOrDefault(emptyList())
    }
}
