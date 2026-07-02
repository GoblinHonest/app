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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

/**
 * 内存 LRU 缓存
 *
 * 线程安全的通用缓存实现，用于缓存网络请求结果。
 * 基于 [LinkedHashMap] 实现 LRU 淘汰策略，
 * 使用 [Mutex] 保证协程安全。
 *
 * @param T 缓存值类型
 * @param maxSize 最大缓存条目数
 * @param ttlMillis 缓存有效期（毫秒），-1 表示永不过期
 */
class LruCache<T>(
    private val maxSize: Int = 100,
    private val ttlMillis: Long = 5 * 60 * 1000L
) {
    private val cache = object : LinkedHashMap<String, CacheEntry<T>>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry<T>>): Boolean {
            return size > maxSize
        }
    }
    private val mutex = Mutex()

    /** 获取缓存值，过期则返回 null */
    suspend fun get(key: String): T? = mutex.withLock {
        val entry = cache[key] ?: return null
        if (ttlMillis > 0 && currentTimeMillis() - entry.timestamp > ttlMillis) {
            cache.remove(key)
            return null
        }
        entry.value
    }

    /** 写入缓存 */
    suspend fun put(key: String, value: T) = mutex.withLock {
        cache[key] = CacheEntry(value, currentTimeMillis())
    }

    /** 清除所有缓存 */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    /** 带缓存的取值：命中直接返回，未命中则执行 [loader] 并缓存结果 */
    suspend fun getOrPut(key: String, loader: suspend () -> T): T {
        get(key)?.let { return it }
        val value = loader()
        put(key, value)
        return value
    }

    private data class CacheEntry<T>(val value: T, val timestamp: Long)
}

/** 跨平台获取当前时间戳（毫秒） */
fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
