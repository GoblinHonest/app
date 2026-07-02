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

package com.stark.miuix.data.storage

import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.model.WatchHistory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 本地持久化存储
 *
 * 将视频源、收藏、历史序列化为 JSON 并写入文件系统。
 * 文件路径由 [StoragePath] 的平台实现决定。
 * 读写操作均在调用方线程执行，建议配合 Dispatchers.Default 使用。
 */
class LocalStorage(private val basePath: String) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    /** 保存视频源列表 */
    fun saveSources(sources: List<VideoSource>) {
        writeFile("$basePath/sources.json", json.encodeToString(sources))
    }

    /** 读取视频源列表 */
    fun loadSources(): List<VideoSource> {
        val content = readFile("$basePath/sources.json") ?: return emptyList()
        return runCatching { json.decodeFromString<List<VideoSource>>(content) }.getOrDefault(emptyList())
    }

    /** 保存收藏列表 */
    fun saveFavorites(favorites: List<Favorite>) {
        writeFile("$basePath/favorites.json", json.encodeToString(favorites))
    }

    /** 读取收藏列表 */
    fun loadFavorites(): List<Favorite> {
        val content = readFile("$basePath/favorites.json") ?: return emptyList()
        return runCatching { json.decodeFromString<List<Favorite>>(content) }.getOrDefault(emptyList())
    }

    /** 保存观看历史 */
    fun saveHistory(history: List<WatchHistory>) {
        writeFile("$basePath/history.json", json.encodeToString(history))
    }

    /** 读取观看历史 */
    fun loadHistory(): List<WatchHistory> {
        val content = readFile("$basePath/history.json") ?: return emptyList()
        return runCatching { json.decodeFromString<List<WatchHistory>>(content) }.getOrDefault(emptyList())
    }
}

/** 跨平台文件读写 */
expect fun writeFile(path: String, content: String)
expect fun readFile(path: String): String?

/** 跨平台获取应用数据目录 */
expect fun getAppDataDir(): String
