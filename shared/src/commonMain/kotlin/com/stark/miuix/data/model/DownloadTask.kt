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

package com.stark.miuix.data.model

import kotlinx.serialization.Serializable

/**
 * 下载任务状态
 */
@Serializable
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

/**
 * 下载任务数据模型
 *
 * @property id 任务唯一标识
 * @property videoId 视频 ID
 * @property title 视频标题
 * @property episodeName 集名称
 * @property url 下载地址
 * @property status 当前状态
 * @property progress 进度 (0.0 ~ 1.0)
 * @property totalBytes 总字节数
 * @property downloadedBytes 已下载字节数
 * @property filePath 本地存储路径
 * @property speedBytesPerSec 当前下载速度
 * @property cover 封面 URL
 * @property sourceName 视频源名称
 * @property createTime 创建时间戳
 */
@Serializable
data class DownloadTask(
    val id: String = "",
    val videoId: String = "",
    val title: String = "",
    val episodeName: String = "",
    val url: String = "",
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Float = 0f,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val filePath: String = "",
    val speedBytesPerSec: Long = 0L,
    val cover: String = "",
    val sourceName: String = "",
    val createTime: Long = 0L
)
