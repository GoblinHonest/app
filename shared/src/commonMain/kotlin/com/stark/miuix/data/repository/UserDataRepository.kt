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

import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.util.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 用户数据仓库 — 观看历史与收藏
 *
 * 通过 [LocalStorage] 自动持久化到本地文件，应用重启后数据恢复。
 */
class UserDataRepository(private val storage: LocalStorage? = null) {

    private val _watchHistory = MutableStateFlow<List<WatchHistory>>(emptyList())
    val watchHistory: StateFlow<List<WatchHistory>> = _watchHistory.asStateFlow()

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()

    init {
        storage?.let {
            _watchHistory.value = it.loadHistory()
            _favorites.value = it.loadFavorites()
        }
    }

    fun addWatchHistory(history: WatchHistory) {
        val current = _watchHistory.value.toMutableList()
        current.removeAll { it.videoId == history.videoId }
        current.add(0, history.copy(timestamp = currentTimeMillis()))
        if (current.size > MAX_HISTORY) current.subList(MAX_HISTORY, current.size).clear()
        _watchHistory.value = current
        storage?.saveHistory(current)
    }

    fun removeHistory(videoId: String) {
        _watchHistory.value = _watchHistory.value.filter { it.videoId != videoId }
        storage?.saveHistory(_watchHistory.value)
    }

    fun clearHistory() {
        _watchHistory.value = emptyList()
        storage?.saveHistory(emptyList())
    }

    fun toggleFavorite(favorite: Favorite): Boolean {
        val current = _favorites.value.toMutableList()
        val existing = current.find { it.videoId == favorite.videoId }
        return if (existing != null) {
            current.remove(existing)
            _favorites.value = current
            storage?.saveFavorites(current)
            false
        } else {
            current.add(0, favorite.copy(timestamp = currentTimeMillis()))
            _favorites.value = current
            storage?.saveFavorites(current)
            true
        }
    }

    fun isFavorite(videoId: String): Boolean = _favorites.value.any { it.videoId == videoId }

    fun removeFavorite(videoId: String) {
        _favorites.value = _favorites.value.filter { it.videoId != videoId }
        storage?.saveFavorites(_favorites.value)
    }

    companion object {
        private const val MAX_HISTORY = 200
    }
}
