/*
 * Copyright 2024 Stark Industries
 *
 * Desktop 平台 SsdpDiscovery 工厂
 */
package com.stark.miuix.data.dlna.ssdp

import com.stark.miuix.util.NetworkClient

actual class SsdpDiscoveryFactory actual constructor(
    private val networkClient: NetworkClient
) {
    actual fun create(): SsdpDiscovery = DesktopSsdpDiscovery(networkClient)
    actual val isPlatformSupported: Boolean = true
}
