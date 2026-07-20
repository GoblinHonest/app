/*
 * Copyright 2024 Stark Industries
 *
 * WasmJs 平台 SSDP 发现实现
 *
 * 浏览器环境无法访问 UDP 套接字，SSDP 多播发现不可行。
 * 返回空流并将 isSupported 置为 false，UI 应据此隐藏投屏入口。
 */
package com.stark.miuix.data.dlna.ssdp

import com.stark.miuix.data.dlna.model.DlnaDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class WasmJsSsdpDiscovery : SsdpDiscovery {
    override val isSupported: Boolean = false
    override fun discover(): Flow<DlnaDevice> = emptyFlow()
}
