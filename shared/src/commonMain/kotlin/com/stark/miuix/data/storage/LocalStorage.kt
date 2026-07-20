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

import com.stark.miuix.data.model.DownloadTask
import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.model.WatchProgress
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

    /** 保存观看进度列表（断点续播） */
    fun saveProgressList(progressList: List<WatchProgress>) {
        writeFile("$basePath/progress.json", json.encodeToString(progressList))
    }

    /** 读取观看进度列表 */
    fun loadProgressList(): List<WatchProgress> {
        val content = readFile("$basePath/progress.json") ?: return emptyList()
        return runCatching { json.decodeFromString<List<WatchProgress>>(content) }.getOrDefault(emptyList())
    }

    /** 保存下载任务列表 */
    fun saveDownloads(downloads: List<DownloadTask>) {
        writeFile("$basePath/downloads.json", json.encodeToString(downloads))
    }

    /** 读取下载任务列表 */
    fun loadDownloads(): List<DownloadTask> {
        val content = readFile("$basePath/downloads.json") ?: return emptyList()
        return runCatching { json.decodeFromString<List<DownloadTask>>(content) }.getOrDefault(emptyList())
    }

    /** 保存用户设置（键值对 JSON） */
    fun saveSettings(settings: Map<String, String>) {
        writeFile("$basePath/settings.json", json.encodeToString(settings))
    }

    /** 读取用户设置 */
    fun loadSettings(): Map<String, String> {
        val content = readFile("$basePath/settings.json") ?: return emptyMap()
        return runCatching { json.decodeFromString<Map<String, String>>(content) }.getOrDefault(emptyMap())
    }

    /** 导出所有用户数据为 JSON 字符串（备份用） */
    fun exportAllData(): String {
        val data = BackupData(
            sources = loadSources(),
            favorites = loadFavorites(),
            history = loadHistory(),
            progressList = loadProgressList(),
            downloads = loadDownloads(),
            settings = loadSettings(),
            version = BACKUP_VERSION
        )
        return json.encodeToString(data)
    }

    /** 从备份 JSON 恢复所有数据 */
    fun importAllData(jsonString: String): Result<Int> = runCatching {
        val data = json.decodeFromString<BackupData>(jsonString)
        saveSources(data.sources)
        saveFavorites(data.favorites)
        saveHistory(data.history)
        saveProgressList(data.progressList)
        saveDownloads(data.downloads)
        saveSettings(data.settings)
        data.sources.size + data.favorites.size + data.history.size
    }
}

/** 备份数据结构 */
@Serializable
private data class BackupData(
    val sources: List<VideoSource> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val history: List<WatchHistory> = emptyList(),
    val progressList: List<WatchProgress> = emptyList(),
    val downloads: List<DownloadTask> = emptyList(),
    val settings: Map<String, String> = emptyMap(),
    val version: Int = 1
)

private const val BACKUP_VERSION = 1

/** 跨平台文件读写 */
expect fun writeFile(path: String, content: String)
expect fun readFile(path: String): String?

/** 跨平台获取应用数据目录 */
expect fun getAppDataDir(): String
