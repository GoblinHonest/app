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

package com.stark.miuix.data.repository

import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.util.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 视频源仓库
 *
 * 管理视频源的增删改查，通过 [LocalStorage] 自动持久化变更。
 * [sources] StateFlow 驱动 UI 列表自动刷新。
 */
class SourceRepository(private val storage: LocalStorage? = null) {

    private val _sources = MutableStateFlow<List<VideoSource>>(emptyList())
    val sources: StateFlow<List<VideoSource>> = _sources.asStateFlow()

    init {
        storage?.let { _sources.value = it.loadSources() }
    }

    fun getEnabledSources(): List<VideoSource> = _sources.value.filter { it.enabled }

    fun getSourceByName(name: String): VideoSource? = _sources.value.find { it.sourceName == name }

    fun addSource(source: VideoSource) {
        val current = _sources.value.toMutableList()
        val idx = current.indexOfFirst { it.sourceName == source.sourceName }
        if (idx >= 0) current[idx] = source else current.add(source)
        _sources.value = current
        persist()
    }

    fun importFromJson(jsonString: String): Result<Int> = runCatching {
        val imported = JsonUtils.decodeVideoSources(jsonString)
        imported.forEach { addSource(it) }
        imported.size
    }

    fun initWithDefaultsIfEmpty(json: String) {
        val defaults = runCatching { JsonUtils.decodeVideoSources(json) }.getOrNull() ?: return
        if (_sources.value.isEmpty()) {
            defaults.forEach { addSource(it) }
        } else {
            // 已有源时：新增不存在的源 + 版本更新已有源
            defaults.forEach { default ->
                val existing = _sources.value.find { it.sourceName == default.sourceName }
                if (existing == null) {
                    addSource(default)
                } else if (default.version > existing.version) {
                    addSource(default)
                }
            }
        }
    }

    fun removeSource(sourceName: String) {
        _sources.value = _sources.value.filter { it.sourceName != sourceName }
        persist()
    }

    fun toggleSource(sourceName: String) {
        _sources.value = _sources.value.map { s ->
            if (s.sourceName == sourceName) s.copy(enabled = !s.enabled) else s
        }
        persist()
    }

    fun exportToJson(): String = JsonUtils.encodeVideoSources(_sources.value)

    private fun persist() {
        storage?.saveSources(_sources.value)
    }
}
