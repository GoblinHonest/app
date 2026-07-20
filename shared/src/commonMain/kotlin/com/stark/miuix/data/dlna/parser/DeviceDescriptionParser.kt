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

package com.stark.miuix.data.dlna.parser

import com.stark.miuix.data.dlna.model.DlnaDevice

/**
 * DLNA 设备描述 XML 解析器
 *
 * UPnP 设备描述文档结构（节选）：
 * ```xml
 * <root xmlns="urn:schemas-upnp-org:device-1-0">
 *   <device>
 *     <UDN>uuid:xxxx-xxxx</UDN>
 *     <friendlyName>小米电视</FriendlyName>
 *     <manufacturer>Xiaomi</manufacturer>
 *     <modelName>TV-2024</modelName>
 *     <iconList>
 *       <icon><mimetype>image/png</mimetype><url>/icon.png</url></icon>
 *     </iconList>
 *     <serviceList>
 *       <service>
 *         <serviceType>urn:schemas-upnp-org:service:AVTransport:1</serviceType>
 *         <serviceId>urn:upnp-org:serviceId:AVTransport</serviceId>
 *         <controlURL>/AVTransport/control</controlURL>
 *         <eventSubURL>/AVTransport/event</eventSubURL>
 *         <SCPDURL>/AVTransport/scpd.xml</SCPDURL>
 *       </service>
 *     </serviceList>
 *   </device>
 * </root>
 * ```
 */
object DeviceDescriptionParser {

    /** AVTransport 服务类型（投屏必需） */
    private const val AV_TRANSPORT_SERVICE_TYPE = "urn:schemas-upnp-org:service:AVTransport:1"

    /**
     * 解析设备描述 XML
     *
     * @param xml 设备描述 XML 文本
     * @param locationUrl 设备 LOCATION URL（用于将 controlURL 的相对路径转为绝对路径）
     * @return 解析得到的 DlnaDevice；若设备不支持 AVTransport 则返回 null
     */
    fun parse(xml: String, locationUrl: String): DlnaDevice? {
        val udn = XmlParser.tagContent(xml, "UDN").removePrefix("uuid:")
        val friendlyName = XmlParser.tagContent(xml, "friendlyName").ifBlank { "未知设备" }
        val manufacturer = XmlParser.tagContent(xml, "manufacturer")
        val modelName = XmlParser.tagContent(xml, "modelName")

        // 查找 AVTransport 服务节点
        val serviceBlocks = XmlParser.allTagBlocks(xml, "service")
        val avTransportBlock = serviceBlocks.firstOrNull { block ->
            val serviceType = XmlParser.tagContent(block, "serviceType")
            serviceType.equals(AV_TRANSPORT_SERVICE_TYPE, ignoreCase = true)
        } ?: return null

        val controlUrl = XmlParser.tagContent(avTransportBlock, "controlURL").ifBlank { return null }
        val absoluteControlUrl = resolveAbsoluteUrl(locationUrl, controlUrl)

        // 取第一个 icon 作为列表图标（可选）
        val iconUrl = parseIconUrl(xml, locationUrl)

        return DlnaDevice(
            udn = udn,
            friendlyName = friendlyName,
            location = locationUrl,
            avTransportControlUrl = absoluteControlUrl,
            manufacturer = manufacturer,
            modelName = modelName,
            iconUrl = iconUrl
        )
    }

    /**
     * 解析设备图标 URL
     *
     * 选择优先级：png > jpeg > 任意；尺寸最接近 48x48 的优先。
     */
    private fun parseIconUrl(xml: String, locationUrl: String): String {
        val iconBlocks = XmlParser.allTagBlocks(xml, "icon")
        if (iconBlocks.isEmpty()) return ""
        // 优先 png，其次 jpeg
        val preferred = iconBlocks.firstOrNull { block ->
            val mime = XmlParser.tagContent(block, "mimetype")
            mime.contains("png", ignoreCase = true)
        } ?: iconBlocks.firstOrNull { block ->
            val mime = XmlParser.tagContent(block, "mimetype")
            mime.contains("jpeg", ignoreCase = true) || mime.contains("jpg", ignoreCase = true)
        } ?: iconBlocks.first()

        val url = XmlParser.tagContent(preferred, "url")
        return if (url.isNotBlank()) resolveAbsoluteUrl(locationUrl, url) else ""
    }

    /**
     * 将相对 URL 解析为绝对 URL
     *
     * @param baseUrl 设备 LOCATION URL（如 http://192.168.1.10:80/desc.xml）
     * @param relativePath 相对路径（如 /AVTransport/control 或 AVTransport/control）
     * @return 绝对 URL（如 http://192.168.1.10:80/AVTransport/control）
     */
    fun resolveAbsoluteUrl(baseUrl: String, relativePath: String): String {
        if (relativePath.isBlank()) return ""
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        // 提取 baseUrl 的 scheme://host:port 部分
        val schemeHostPort = Regex("(https?://[^/]+)").find(baseUrl)?.groupValues?.getOrNull(1)
            ?: return relativePath
        return schemeHostPort + if (relativePath.startsWith("/")) relativePath else "/$relativePath"
    }
}
