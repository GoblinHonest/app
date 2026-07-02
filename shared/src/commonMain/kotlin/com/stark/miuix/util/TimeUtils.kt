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

/**
 * 时间格式化工具
 *
 * 提供相对时间描述（如"刚刚"、"3 分钟前"、"昨天"），
 * 适用于观看历史和收藏列表的时间标注。
 */
object TimeUtils {

    /**
     * 将时间戳格式化为相对时间描述
     *
     * @param timestamp 毫秒时间戳
     * @return 人类可读的相对时间（如"刚刚"、"5 分钟前"、"3 天前"）
     */
    fun formatRelative(timestamp: Long): String {
        val now = currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < MINUTE -> "刚刚"
            diff < HOUR -> "${diff / MINUTE} 分钟前"
            diff < DAY -> "${diff / HOUR} 小时前"
            diff < DAY * 2 -> "昨天"
            diff < WEEK -> "${diff / DAY} 天前"
            diff < MONTH -> "${diff / WEEK} 周前"
            else -> "${diff / MONTH} 个月前"
        }
    }

    private const val MINUTE = 60_000L
    private const val HOUR = 3_600_000L
    private const val DAY = 86_400_000L
    private const val WEEK = 604_800_000L
    private const val MONTH = 2_592_000_000L
}
