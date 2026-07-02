/*
 * Copyright 2024 Starter
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

package com.stark.miuix.data.repository

import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.util.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 视频源仓库
 *
 * 管理视频源的增删改查操作，维护内存中的视频源列表状态。
 * 提供 StateFlow 供 UI 层观察视频源列表变化。
 */
class SourceRepository {

    private val _sources = MutableStateFlow<List<VideoSource>>(emptyList())

    /** 当前所有视频源列表 */
    val sources: StateFlow<List<VideoSource>> = _sources.asStateFlow()

    /**
     * 获取所有已启用的视频源
     *
     * @return 已启用的视频源列表
     */
    fun getEnabledSources(): List<VideoSource> {
        return _sources.value.filter { it.enabled }
    }

    /**
     * 根据名称获取视频源
     *
     * @param name 源名称
     * @return 匹配的视频源，未找到返回 null
     */
    fun getSourceByName(name: String): VideoSource? {
        return _sources.value.find { it.sourceName == name }
    }

    /**
     * 添加视频源
     *
     * 如果已存在同名源，将更新为新版本。
     *
     * @param source 要添加的视频源
     */
    fun addSource(source: VideoSource) {
        val current = _sources.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.sourceName == source.sourceName }
        if (existingIndex >= 0) {
            current[existingIndex] = source
        } else {
            current.add(source)
        }
        _sources.value = current
    }

    /**
     * 从 JSON 字符串导入视频源
     *
     * 支持单个源（JSON Object）和批量源（JSON Array）两种格式。
     *
     * @param jsonString JSON 格式的视频源配置
     * @return 成功导入的源数量
     */
    fun importFromJson(jsonString: String): Result<Int> {
        return runCatching {
            val imported = JsonUtils.decodeVideoSources(jsonString)
            imported.forEach { addSource(it) }
            imported.size
        }
    }

    /**
     * 删除视频源
     *
     * @param sourceName 要删除的源名称
     */
    fun removeSource(sourceName: String) {
        _sources.value = _sources.value.filter { it.sourceName != sourceName }
    }

    /**
     * 切换视频源启用状态
     *
     * @param sourceName 源名称
     */
    fun toggleSource(sourceName: String) {
        _sources.value = _sources.value.map { source ->
            if (source.sourceName == sourceName) {
                source.copy(enabled = !source.enabled)
            } else {
                source
            }
        }
    }

    /**
     * 导出所有视频源为 JSON 字符串
     *
     * @return JSON 格式的视频源配置
     */
    fun exportToJson(): String {
        return JsonUtils.encodeVideoSources(_sources.value)
    }
}
