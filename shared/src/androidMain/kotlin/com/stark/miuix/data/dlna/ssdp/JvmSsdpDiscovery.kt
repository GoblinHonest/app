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
import com.stark.miuix.data.dlna.parser.DeviceDescriptionParser
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket

/**
 * Android 平台 SSDP 设备发现实现
 *
 * 基于 java.net.MulticastSocket 实现 UDP 多播 M-SEARCH + 单播应答监听。
 *
 * 工作流程：
 * 1. 创建 MulticastSocket 绑定本地任意端口
 * 2. 设置 SO_TIMEOUT 控制监听退出
 * 3. 向 239.255.255.250:1900 发送 M-SEARCH 包
 * 4. 在 DISCOVERY_TIMEOUT_MS 内循环接收应答
 * 5. 解析 LOCATION → HTTP GET 拉取设备描述 XML → 解析为 DlnaDevice
 * 6. 通过 Flow 推送每个新设备（按 stableKey 去重）
 *
 * 注意：调用方需在调用前申请 android.permission.CHANGE_WIFI_MULTICAST_STATE，
 * 并通过 WifiManager.MulticastLock 释放组播限制。本类不处理权限，
 * 由 [com.stark.miuix.data.dlna.DlnaController] 在 App 层统一管理。
 */
class JvmSsdpDiscovery(
    private val networkClient: NetworkClient
) : SsdpDiscovery {

    override val isSupported: Boolean = true

    override fun discover(): Flow<DlnaDevice> = flow {
        val discoveredKeys = mutableSetOf<String>()
        val requestBytes = SsdpConstants.buildMSearchPacket().toByteArray(Charsets.UTF_8)
        val groupAddress = InetAddress.getByName(SsdpConstants.MULTICAST_ADDRESS)

        withContext(Dispatchers.IO) {
            // MulticastSocket 必须使用 try-with-resources 确保释放
            MulticastSocket().use { socket ->
                socket.soTimeout = SsdpConstants.DISCOVERY_TIMEOUT_MS.toInt()
                socket.reuseAddress = true
                socket.timeToLive = 4

                // 发送 M-SEARCH 多播包
                val sendPacket = DatagramPacket(
                    requestBytes, requestBytes.size,
                    groupAddress, SsdpConstants.MULTICAST_PORT
                )
                runCatching { socket.send(sendPacket) }

                val receiveBuffer = ByteArray(8192)
                val startTime = System.currentTimeMillis()

                // 在超时窗口内循环接收
                while (System.currentTimeMillis() - startTime < SsdpConstants.DISCOVERY_TIMEOUT_MS) {
                    val responsePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                    val received = runCatching {
                        socket.receive(responsePacket)
                        true
                    }.getOrDefault(false)
                    if (!received) continue  // SO_TIMEOUT 触发

                    val responseText = String(responsePacket.data, 0, responsePacket.length, Charsets.UTF_8)
                    val location = SsdpConstants.parseLocation(responseText) ?: continue

                    // 拉取设备描述 XML 并解析
                    val device = runCatching {
                        val xml = networkClient.get(location)
                        DeviceDescriptionParser.parse(xml, location)
                    }.getOrNull() ?: continue

                    if (discoveredKeys.add(device.stableKey)) {
                        emit(device)
                    }
                }
            }
        }
    }
}
