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

package com.stark.miuix.data.sync

import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.util.AppLogger
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 多设备数据同步管理器
 *
 * 支持通过 WebDAV 协议将用户数据（源、收藏、历史、进度）
 * 同步到坚果云/Nextcloud 等 WebDAV 服务。
 *
 * 同步策略：
 * - 上传：将本地备份 JSON PUT 到 WebDAV 路径
 * - 下载：从 WebDAV 路径 GET 备份 JSON 并恢复
 * - 冲突：以最新 timestamp 为准
 */
class SyncManager(
    private val storage: LocalStorage,
    private val networkClient: NetworkClient
) {
    private var webdavUrl: String = ""
    private var username: String = ""
    private var password: String = ""

    /** 配置 WebDAV 连接 */
    fun configure(url: String, user: String, pass: String) {
        webdavUrl = url.trimEnd('/')
        username = user
        password = pass
    }

    /** 是否已配置 */
    val isConfigured: Boolean get() = webdavUrl.isNotBlank() && username.isNotBlank()

    /**
     * 上传本地数据到 WebDAV
     */
    suspend fun upload(): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            if (!isConfigured) throw IllegalStateException("WebDAV not configured")
            val data = storage.exportAllData()
            val targetUrl = webdavUrl + "/" + BACKUP_FILENAME
            networkClient.post(
                url = targetUrl,
                body = data,
                headers = buildAuthHeaders() + mapOf("Content-Type" to "application/json")
            )
            AppLogger.d("Sync", "upload success")
        }
    }

    /**
     * 从 WebDAV 下载并恢复数据
     */
    suspend fun download(): Result<Int> = withContext(Dispatchers.Default) {
        runCatching {
            if (!isConfigured) throw IllegalStateException("WebDAV not configured")
            val targetUrl = webdavUrl + "/" + BACKUP_FILENAME
            val data = networkClient.get(targetUrl, headers = buildAuthHeaders())
            storage.importAllData(data).getOrThrow()
        }
    }

    private fun buildAuthHeaders(): Map<String, String> {
        val credentials = encodeBase64("$username:$password".encodeToByteArray())
        return mapOf("Authorization" to "Basic $credentials")
    }

    private fun encodeBase64(data: ByteArray): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val sb = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0
            sb.append(chars[b0 shr 2])
            sb.append(chars[((b0 and 3) shl 4) or (b1 shr 4)])
            sb.append(if (i + 1 < data.size) chars[((b1 and 15) shl 2) or (b2 shr 6)] else '=')
            sb.append(if (i + 2 < data.size) chars[b2 and 63] else '=')
            i += 3
        }
        return sb.toString()
    }

    companion object {
        private const val BACKUP_FILENAME = "cinehub_backup.json"
    }
}
