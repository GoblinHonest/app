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
import com.stark.miuix.util.NetworkClient

/**
 * Android 平台 SsdpDiscovery 工厂
 *
 * 投屏功能仅在 Android 全屏播放器暴露，桌面平台即便支持 SSDP
 * 也无播放器入口。但保留实现以便未来扩展。
 */
actual class SsdpDiscoveryFactory actual constructor(
    private val networkClient: NetworkClient
) {
    actual fun create(): SsdpDiscovery = JvmSsdpDiscovery(networkClient)
    actual val isPlatformSupported: Boolean = true
}
