/*
 * Copyright 2024 Stark Industries
 *
 * Desktop (JVM) 平台 SSDP 发现实现
 *
 * 与 Android 共用 java.net.MulticastSocket 方案。
 * Desktop 平台无投屏 UI 入口（投屏按钮仅 Android 全屏播放器显示），
 * 此实现保留以备未来扩展。
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
import java.net.MulticastSocket

class DesktopSsdpDiscovery(
    private val networkClient: NetworkClient
) : SsdpDiscovery {

    override val isSupported: Boolean = true

    override fun discover(): Flow<DlnaDevice> = flow {
        val discoveredKeys = mutableSetOf<String>()
        val requestBytes = SsdpConstants.buildMSearchPacket().toByteArray(Charsets.UTF_8)
        val groupAddress = InetAddress.getByName(SsdpConstants.MULTICAST_ADDRESS)

        withContext(Dispatchers.IO) {
            MulticastSocket().use { socket ->
                socket.soTimeout = SsdpConstants.DISCOVERY_TIMEOUT_MS.toInt()
                socket.reuseAddress = true
                socket.timeToLive = 4

                val sendPacket = DatagramPacket(
                    requestBytes, requestBytes.size,
                    groupAddress, SsdpConstants.MULTICAST_PORT
                )
                runCatching { socket.send(sendPacket) }

                val receiveBuffer = ByteArray(8192)
                val startTime = System.currentTimeMillis()

                while (System.currentTimeMillis() - startTime < SsdpConstants.DISCOVERY_TIMEOUT_MS) {
                    val responsePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                    val received = runCatching {
                        socket.receive(responsePacket)
                        true
                    }.getOrDefault(false)
                    if (!received) continue

                    val responseText = String(responsePacket.data, 0, responsePacket.length, Charsets.UTF_8)
                    val location = SsdpConstants.parseLocation(responseText) ?: continue

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
