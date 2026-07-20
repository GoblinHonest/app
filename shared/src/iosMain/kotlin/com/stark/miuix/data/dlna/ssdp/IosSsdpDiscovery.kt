/*
 * Copyright 2024 Stark Industries
 *
 * iOS 平台 SSDP 发现实现
 *
 * TODO: 使用 Network.framework NWConnection 实现 UDP 多播。
 * 当前先返回空流并标记不支持，保证编译通过；
 * 待 mac 环境下用 NWConnection 完整实现后替换此文件。
 *
 * 实现思路（待补完）：
 * - NWConnection(host: "239.255.255.250", port: 1900, using: .udp)
 * - 设置 connectionGroup（多播组）
 * - send M-SEARCH 包
 * - receive 循环拉取应答
 */
package com.stark.miuix.data.dlna.ssdp

import com.stark.miuix.data.dlna.model.DlnaDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosSsdpDiscovery : SsdpDiscovery {
    override val isSupported: Boolean = false
    override fun discover(): Flow<DlnaDevice> = emptyFlow()
}
