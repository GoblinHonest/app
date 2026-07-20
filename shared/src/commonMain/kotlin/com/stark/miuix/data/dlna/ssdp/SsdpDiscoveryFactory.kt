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

import com.stark.miuix.util.NetworkClient

/**
 * SSDP 发现工厂（expect 声明）
 *
 * 由各平台 actual 实现：
 * - Android/Desktop: 返回基于 MulticastSocket 的真实实现
 * - iOS: 返回基于 Network.framework 的真实实现（待补完）
 * - WasmJs: 返回 NoOpSsdpDiscovery（浏览器无 UDP 能力）
 *
 * 调用方通过 [isPlatformSupported] 判断是否启用投屏 UI 入口。
 */
expect class SsdpDiscoveryFactory(networkClient: NetworkClient) {
    /** 创建平台对应的 SsdpDiscovery 实例 */
    fun create(): SsdpDiscovery

    /** 当前平台是否支持 SSDP 发现 */
    val isPlatformSupported: Boolean
}
