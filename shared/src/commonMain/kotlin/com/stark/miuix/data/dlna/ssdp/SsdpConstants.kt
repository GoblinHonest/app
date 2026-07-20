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

package com.stark.miuix.data.dlna.ssdp

/**
 * SSDP (Simple Service Discovery Protocol) 协议常量
 *
 * DLNA/UPnP 设备发现基于 SSDP，使用 UDP 多播：
 * - 多播地址：239.255.255.250
 * - 端口：1900
 *
 * 客户端通过发送 M-SEARCH 主动发现设备，设备在 1900 端口回单播应答。
 */
object SsdpConstants {
    /** SSDP 多播组地址 */
    const val MULTICAST_ADDRESS = "239.255.255.250"

    /** SSDP 标准端口 */
    const val MULTICAST_PORT = 1900

    /** M-SEARCH 发现超时（毫秒） */
    const val DISCOVERY_TIMEOUT_MS = 5000L

    /** MediaRenderer 设备类型（DLNA 渲染器） */
    const val SEARCH_TARGET_MEDIA_RENDERER = "urn:schemas-upnp-org:device:MediaRenderer:1"

    /** 通配搜索目标（所有 UPnP 设备都会应答） */
    const val SEARCH_TARGET_ALL = "ssdp:all"

    /**
     * 构造 M-SEARCH 请求包
     *
     * @param searchTarget 搜索目标，默认仅搜索 MediaRenderer
     * @return SSDP M-SEARCH 请求字符串（CRLF 结尾的字节序列）
     */
    fun buildMSearchPacket(searchTarget: String = SEARCH_TARGET_MEDIA_RENDERER): String {
        return buildString {
            append("M-SEARCH * HTTP/1.1\r\n")
            append("HOST: $MULTICAST_ADDRESS:$MULTICAST_PORT\r\n")
            append("MAN: \"ssdp:discover\"\r\n")
            append("MX: 2\r\n")
            append("ST: $searchTarget\r\n")
            append("USER-AGENT: miuix-video/1.0\r\n")
            append("\r\n")
        }
    }

    /**
     * 解析 SSDP 响应包，提取 LOCATION 头
     *
     * @param response 设备返回的 SSDP 应答字符串
     * @return LOCATION URL，未找到返回 null
     */
    fun parseLocation(response: String): String? {
        val regex = Regex("(?i)^LOCATION:\\s*(.+)$", RegexOption.MULTILINE)
        return regex.find(response)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
    }
}
