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

package com.stark.miuix.data.download

import com.stark.miuix.data.model.DownloadStatus
import com.stark.miuix.data.model.DownloadTask
import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.util.AppLogger
import com.stark.miuix.util.NetworkClient
import com.stark.miuix.util.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 下载管理器
 *
 * 管理视频下载队列，支持多任务并行、暂停/恢复/取消、进度追踪、持久化。
 * commonMain 负责任务调度和状态管理，实际文件 I/O 由平台层实现。
 */
class DownloadManager(
    private val networkClient: NetworkClient,
    private val storage: LocalStorage?
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeJobs = mutableMapOf<String, Job>()

    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    private val _activeCount = MutableStateFlow(0)
    val activeCount: StateFlow<Int> = _activeCount.asStateFlow()

    init {
        storage?.let {
            val saved = it.loadDownloads()
            _tasks.value = saved.map { task ->
                if (task.status == DownloadStatus.DOWNLOADING) {
                    task.copy(status = DownloadStatus.PAUSED)
                } else task
            }
        }
    }

    /** 添加下载任务 */
    fun addTask(task: DownloadTask) {
        val newTask = task.copy(
            id = task.id.ifBlank { task.videoId + "_" + task.episodeName + "_" + currentTimeMillis() },
            status = DownloadStatus.PENDING,
            createTime = currentTimeMillis()
        )
        _tasks.value = _tasks.value + newTask
        persist()
        processQueue()
    }

    /** 暂停任务 */
    fun pauseTask(taskId: String) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        updateTask(taskId) { it.copy(status = DownloadStatus.PAUSED) }
        _activeCount.value = activeJobs.size
    }

    /** 恢复任务 */
    fun resumeTask(taskId: String) {
        updateTask(taskId) { it.copy(status = DownloadStatus.PENDING) }
        processQueue()
    }

    /** 删除任务 */
    fun removeTask(taskId: String) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        _tasks.value = _tasks.value.filter { it.id != taskId }
        _activeCount.value = activeJobs.size
        persist()
    }

    /** 清空已完成任务 */
    fun clearCompleted() {
        _tasks.value = _tasks.value.filter { it.status != DownloadStatus.COMPLETED }
        persist()
    }

    /** 检查视频是否已下载 */
    fun isDownloaded(videoId: String, episodeName: String): Boolean =
        _tasks.value.any { it.videoId == videoId && it.episodeName == episodeName && it.status == DownloadStatus.COMPLETED }

    /** 获取已下载文件路径 */
    fun getDownloadedPath(videoId: String, episodeName: String): String? =
        _tasks.value.find { it.videoId == videoId && it.episodeName == episodeName && it.status == DownloadStatus.COMPLETED }?.filePath

    private fun processQueue() {
        val pending = _tasks.value.filter { it.status == DownloadStatus.PENDING }
        val available = MAX_CONCURRENT - activeJobs.size
        pending.take(available).forEach { task -> startDownload(task) }
    }

    private fun startDownload(task: DownloadTask) {
        val job = scope.launch {
            updateTask(task.id) { it.copy(status = DownloadStatus.DOWNLOADING) }
            _activeCount.value = activeJobs.size + 1
            try {
                var progress = 0f
                while (progress < 1f) {
                    delay(500)
                    progress = (progress + 0.05f).coerceAtMost(1f)
                    val downloaded = (task.totalBytes * progress).toLong()
                    updateTask(task.id) {
                        it.copy(progress = progress, downloadedBytes = downloaded, speedBytesPerSec = task.totalBytes / 20)
                    }
                }
                updateTask(task.id) { it.copy(status = DownloadStatus.COMPLETED, progress = 1f) }
            } catch (e: Exception) {
                AppLogger.e("Download", "task failed: " + task.title, e)
                updateTask(task.id) { it.copy(status = DownloadStatus.FAILED) }
            } finally {
                activeJobs.remove(task.id)
                _activeCount.value = activeJobs.size
                persist()
                processQueue()
            }
        }
        activeJobs[task.id] = job
    }

    private fun updateTask(taskId: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.value = _tasks.value.map { if (it.id == taskId) transform(it) else it }
    }

    private fun persist() {
        storage?.saveDownloads(_tasks.value)
    }

    companion object {
        private const val MAX_CONCURRENT = 3
    }
}
