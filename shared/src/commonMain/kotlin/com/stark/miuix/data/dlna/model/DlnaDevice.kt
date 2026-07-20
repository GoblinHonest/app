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

package com.stark.miuix.data.dlna.model

/**
 * DLNA 投屏设备
 *
 * 由 SSDP 发现并通过设备描述 XML 解析得到的可用渲染设备。
 *
 * @property udn 设备唯一标识（UUID 形式）
 * @property friendlyName 用户可见名称（如"小米电视-客厅"）
 * @property location 设备描述 XML 的 URL
 * @property avTransportControlUrl AVTransport 服务控制端点（绝对 URL）
 * @property manufacturer 厂商
 * @property modelName 设备型号
 * @property iconUrl 设备图标 URL（可选，UI 列表展示用）
 */
data class DlnaDevice(
    val udn: String,
    val friendlyName: String,
    val location: String,
    val avTransportControlUrl: String,
    val manufacturer: String = "",
    val modelName: String = "",
    val iconUrl: String = ""
) {
    /**
     * 用于 UI 列表去重的稳定 key。
     * 优先 UDN，回退 controlUrl，再回退 location。
     */
    val stableKey: String get() = udn.ifBlank { avTransportControlUrl.ifBlank { location } }
}
