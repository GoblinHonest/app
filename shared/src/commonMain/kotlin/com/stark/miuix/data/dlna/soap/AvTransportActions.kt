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

package com.stark.miuix.data.dlna.soap

/**
 * AVTransport 服务 SOAP Action 模板
 *
 * UPnP AVTransport:1 服务定义了一组标准 action 用于控制媒体渲染：
 * - SetAVTransportURI: 设置播放地址（投屏第一步）
 * - Play: 开始播放
 * - Pause: 暂停
 * - Stop: 停止并清空队列
 * - Seek: 跳转进度
 * - GetPositionInfo: 查询当前进度（轮询用）
 * - GetTransportInfo: 查询播放状态（Playing/Paused/Stopped）
 *
 * 每个 action 的 SOAP envelope 结构一致，仅 action 名和参数不同。
 *
 * SOAPAction header 值格式："urn:schemas-upnp-org:service:AVTransport:1#ActionName"
 */
object AvTransportActions {

    /** AVTransport 服务类型 URN */
    const val SERVICE_TYPE = "urn:schemas-upnp-org:service:AVTransport:1"

    /** 构造 SOAPAction header */
    fun soapAction(actionName: String): String = "$SERVICE_TYPE#$actionName"

    /**
     * SetAVTransportURI — 设置播放媒体地址
     *
     * @param mediaUrl 媒体 URL（设备会从该地址拉流）
     * @param title 媒体标题（部分设备会在 UI 上显示）
     */
    fun setAVTransportURI(mediaUrl: String, title: String = ""): String {
        val meta = buildDidlLite(mediaUrl, title)
        return envelope("SetAVTransportURI", """
            <InstanceID>0</InstanceID>
            <CurrentURI>${escape(mediaUrl)}</CurrentURI>
            <CurrentURIMetaData>${escape(meta)}</CurrentURIMetaData>
        """.trimIndent())
    }

    /** Play — 开始/继续播放 */
    fun play(): String = envelope("Play", """
        <InstanceID>0</InstanceID>
        <Speed>1</Speed>
    """.trimIndent())

    /** Pause — 暂停 */
    fun pause(): String = envelope("Pause", """
        <InstanceID>0</InstanceID>
    """.trimIndent())

    /** Stop — 停止播放 */
    fun stop(): String = envelope("Stop", """
        <InstanceID>0</InstanceID>
    """.trimIndent())

    /**
     * Seek — 进度跳转
     * @param target 目标位置，REL_TIME 格式（如 "0:00:30"）或秒数
     */
    fun seek(target: String): String = envelope("Seek", """
        <InstanceID>0</InstanceID>
        <Unit>REL_TIME</Unit>
        <Target>${escape(target)}</Target>
    """.trimIndent())

    /** GetPositionInfo — 查询当前播放进度 */
    fun getPositionInfo(): String = envelope("GetPositionInfo", """
        <InstanceID>0</InstanceID>
    """.trimIndent())

    /** GetTransportInfo — 查询当前播放状态 */
    fun getTransportInfo(): String = envelope("GetTransportInfo", """
        <InstanceID>0</InstanceID>
    """.trimIndent())

    // ===== 内部工具 =====

    /**
     * 构造 SOAP envelope
     *
     * UPnP SOAP 1.1 envelope 标准结构：
     * ```xml
     * <?xml version="1.0" encoding="utf-8"?>
     * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"
     *             s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
     *   <s:Body>
     *     <u:ActionName xmlns:u="urn:schemas-upnp-org:service:AVTransport:1">
     *       ...参数...
     *     </u:ActionName>
     *   </s:Body>
     * </s:Envelope>
     * ```
     */
    private fun envelope(actionName: String, bodyArgs: String): String {
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
            append("s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">")
            append("<s:Body>")
            append("<u:$actionName xmlns:u=\"$SERVICE_TYPE\">")
            append(bodyArgs)
            append("</u:$actionName>")
            append("</s:Body>")
            append("</s:Envelope>")
        }
    }

    /**
     * 构造 DIDL-Lite 元数据
     *
     * 部分设备（如小米电视）要求 CurrentURIMetaData 字段非空，
     * 否则 SetAVTransportURI 会失败。这里构造一个最小可用的 DIDL-Lite。
     */
    private fun buildDidlLite(mediaUrl: String, title: String): String {
        val safeTitle = escape(title.ifBlank { "Video" })
        val safeUrl = escape(mediaUrl)
        return buildString {
            append("<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" ")
            append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ")
            append("xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">")
            append("<item id=\"1\" parentID=\"0\" restricted=\"1\">")
            append("<dc:title>$safeTitle</dc:title>")
            append("<upnp:class>object.item.videoItem</upnp:class>")
            append("<res protocolInfo=\"http-get:*:video/mp4:*\">$safeUrl</res>")
            append("</item>")
            append("</DIDL-Lite>")
        }
    }

    /** XML 字符转义 */
    private fun escape(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
