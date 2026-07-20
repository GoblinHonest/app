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

import com.stark.miuix.data.dlna.model.DlnaDevice
import com.stark.miuix.data.dlna.parser.XmlParser
import com.stark.miuix.util.NetworkClient

/**
 * AVTransport SOAP 控制客户端
 *
 * 封装与 DLNA 设备 AVTransport 服务的 SOAP/HTTP 交互。
 * 所有方法均为 suspend，调用方应在协程中调用。
 *
 * 协议要点：
 * - HTTP POST 到设备的 controlUrl
 * - Content-Type: text/xml; charset="utf-8"
 * - SOAPAction: urn:schemas-upnp-org:service:AVTransport:1#ActionName
 * - Body 为 SOAP envelope XML
 *
 * 错误处理：
 * - HTTP 错误抛出 [SoapException]，包含设备返回的 errorCode/description
 * - 网络错误透传 NetworkClient 的异常
 */
class SoapClient(
    private val networkClient: NetworkClient
) {

    /**
     * 投屏 — 设置媒体地址并立即播放
     *
     * 等价于先 SetAVTransportURI，再 Play。
     * 多数设备要求两步分别调用。
     *
     * @param device 目标设备
     * @param mediaUrl 媒体 URL（设备需能直接访问，局域网可达）
     * @param title 媒体标题（设备 UI 显示用）
     */
    suspend fun cast(device: DlnaDevice, mediaUrl: String, title: String) {
        // 1. 设置 URI
        val setUriBody = AvTransportActions.setAVTransportURI(mediaUrl, title)
        val setUriResp = post(device.avTransportControlUrl, setUriBody, AvTransportActions.soapAction("SetAVTransportURI"))
        checkSoapFault(setUriResp)

        // 2. 立即播放
        val playBody = AvTransportActions.play()
        val playResp = post(device.avTransportControlUrl, playBody, AvTransportActions.soapAction("Play"))
        checkSoapFault(playResp)
    }

    /** 暂停 */
    suspend fun pause(device: DlnaDevice) {
        val resp = post(
            device.avTransportControlUrl,
            AvTransportActions.pause(),
            AvTransportActions.soapAction("Pause")
        )
        checkSoapFault(resp)
    }

    /** 继续播放 */
    suspend fun resume(device: DlnaDevice) {
        val resp = post(
            device.avTransportControlUrl,
            AvTransportActions.play(),
            AvTransportActions.soapAction("Play")
        )
        checkSoapFault(resp)
    }

    /** 停止播放 */
    suspend fun stop(device: DlnaDevice) {
        val resp = post(
            device.avTransportControlUrl,
            AvTransportActions.stop(),
            AvTransportActions.soapAction("Stop")
        )
        checkSoapFault(resp)
    }

    /**
     * 跳转进度
     * @param targetSeconds 目标位置（秒）
     */
    suspend fun seek(device: DlnaDevice, targetSeconds: Long) {
        val target = formatRelTime(targetSeconds)
        val resp = post(
            device.avTransportControlUrl,
            AvTransportActions.seek(target),
            AvTransportActions.soapAction("Seek")
        )
        checkSoapFault(resp)
    }

    /**
     * 查询当前播放进度
     *
     * @return 包含当前位置（毫秒）和总时长（毫秒）的 [PositionInfo]；
     *         设备未返回时字段为 0
     */
    suspend fun getPositionInfo(device: DlnaDevice): PositionInfo {
        val resp = post(
            device.avTransportControlUrl,
            AvTransportActions.getPositionInfo(),
            AvTransportActions.soapAction("GetPositionInfo")
        )
        checkSoapFault(resp)
        return parsePositionInfo(resp)
    }

    /**
     * 查询当前播放状态
     * @return "PLAYING" / "PAUSED_PLAYBACK" / "STOPPED" / "TRANSITIONING" / "NO_MEDIA_PRESENT"
     */
    suspend fun getTransportState(device: DlnaDevice): String {
        val resp = post(
            device.avTransportControlUrl,
            AvTransportActions.getTransportInfo(),
            AvTransportActions.soapAction("GetTransportInfo")
        )
        checkSoapFault(resp)
        return XmlParser.tagContent(resp, "CurrentTransportState")
    }

    // ===== 内部工具 =====

    /** 发送 SOAP 请求并返回响应体 */
    private suspend fun post(url: String, body: String, soapAction: String): String {
        return networkClient.post(
            url = url,
            body = body,
            headers = mapOf(
                "Content-Type" to "text/xml; charset=\"utf-8\"",
                "SOAPAction" to soapAction
            )
        )
    }

    /** 检查 SOAP fault，存在则抛出 SoapException */
    private fun checkSoapFault(response: String) {
        val faultCode = XmlParser.tagContent(response, "errorCode")
        if (faultCode.isNotBlank()) {
            val description = XmlParser.tagContent(response, "errorDescription")
            throw SoapException(faultCode, description.ifBlank { "未知错误" })
        }
    }

    /** 解析 GetPositionInfo 响应 */
    private fun parsePositionInfo(response: String): PositionInfo {
        val relTime = XmlParser.tagContent(response, "RelTime")
        val trackDuration = XmlParser.tagContent(response, "TrackDuration")
        return PositionInfo(
            positionMs = parseHmsToMs(relTime),
            durationMs = parseHmsToMs(trackDuration)
        )
    }

    /**
     * 解析 H:MM:SS 格式时间为毫秒
     * 支持格式：0:00:30 / 1:23:45 / 0:00:00.500（带小数）
     * 返回 -1 表示不可用（设备返回 "NOT_IMPLEMENTED"）
     */
    private fun parseHmsToMs(hms: String): Long {
        if (hms.isBlank() || hms.equals("NOT_IMPLEMENTED", ignoreCase = true)) return -1L
        val parts = hms.split(":")
        if (parts.size != 3) return 0L
        return runCatching {
            val h = parts[0].toLong()
            val m = parts[1].toLong()
            val s = parts[2].toFloat()
            ((h * 3600 + m * 60) * 1000 + (s * 1000).toLong())
        }.getOrDefault(0L)
    }

    /** 将秒数格式化为 H:MM:SS */
    private fun formatRelTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "$h:${"%02d".format(m)}:${"%02d".format(s)}"
    }
}

/** 播放进度信息 */
data class PositionInfo(
    /** 当前位置（毫秒），-1 表示不可用 */
    val positionMs: Long,
    /** 总时长（毫秒），-1 表示不可用 */
    val durationMs: Long
)

/** SOAP 调用异常 */
class SoapException(
    val code: String,
    val description: String
) : Exception("DLNA SOAP error [$code]: $description")
