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
 * 字符串工具类
 *
 * 提供常用的字符串处理功能。
 */
object StringUtils {

    /**
     * 截断字符串到指定长度，超出部分用省略号替代
     *
     * @param input 原始字符串
     * @param maxLength 最大长度（包含省略号）
     * @return 截断后的字符串
     */
    fun truncate(input: String, maxLength: Int): String {
        if (input.length <= maxLength) return input
        return input.take(maxLength - 3) + "..."
    }

    /**
     * 从 URL 中提取域名
     *
     * @param url 完整 URL
     * @return 域名部分，解析失败返回原始 URL
     */
    fun extractDomain(url: String): String {
        return try {
            val trimmed = url.removePrefix("https://").removePrefix("http://")
            trimmed.substringBefore("/").substringBefore(":")
        } catch (_: Exception) {
            url
        }
    }

    /**
     * 判断字符串是否为有效的 URL
     *
     * @param str 待检测字符串
     * @return 是否为有效 URL
     */
    fun isValidUrl(str: String): Boolean {
        return str.startsWith("http://") || str.startsWith("https://")
    }

    /**
     * 将文件大小格式化为人类可读字符串
     *
     * @param bytes 字节数
     * @return 格式化后的大小字符串（如 "1.5 MB"）
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "${bytes} ${units[unitIndex]}"
        } else {
            "%.1f %s".format(size, units[unitIndex])
        }
    }
}
