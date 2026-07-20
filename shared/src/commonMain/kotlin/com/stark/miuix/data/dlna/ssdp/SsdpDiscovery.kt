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

import com.stark.miuix.data.dlna.model.DlnaDevice
import kotlinx.coroutines.flow.Flow

/**
 * SSDP 设备发现接口（expect 声明）
 *
 * 各平台基于自身网络栈实现 UDP 多播发现：
 * - Android: java.net.MulticastSocket
 * - Desktop: java.nio.channels.DatagramChannel
 * - iOS: Network.framework NWConnection
 * - WasmJs: 浏览器无 UDP 能力，返回空流
 *
 * 实现要点：
 * 1. 发送 M-SEARCH 包到 [SsdpConstants.MULTICAST_ADDRESS]:[SsdpConstants.MULTICAST_PORT]
 * 2. 在 [SsdpConstants.DISCOVERY_TIMEOUT_MS] 内监听单播应答
 * 3. 解析 LOCATION → 拉取设备描述 XML → 解析出 DlnaDevice
 * 4. 通过 Flow 持续推送新发现的设备（同一设备只推送一次）
 */
interface SsdpDiscovery {
    /**
     * 启动设备发现
     *
     * @return 设备流，在发现超时后自动结束
     */
    fun discover(): Flow<DlnaDevice>

    /**
     * 当前平台是否支持 SSDP 发现
     *
     * WasmJs 等无 UDP 能力的平台返回 false，UI 应据此隐藏投屏入口
     */
    val isSupported: Boolean
}
