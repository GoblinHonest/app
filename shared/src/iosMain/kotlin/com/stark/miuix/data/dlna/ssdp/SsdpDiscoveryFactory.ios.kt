/*
 * Copyright 2024 Stark Industries
 *
 * iOS 平台 SsdpDiscovery 工厂
 */
package com.stark.miuix.data.dlna.ssdp

import com.stark.miuix.util.NetworkClient

actual class SsdpDiscoveryFactory actual constructor(
    private val networkClient: NetworkClient
) {
    actual fun create(): SsdpDiscovery = IosSsdpDiscovery()
    actual val isPlatformSupported: Boolean = false
}
