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

package com.stark.miuix.data.dlna

import com.stark.miuix.data.dlna.model.DlnaDevice
import com.stark.miuix.data.dlna.soap.PositionInfo
import com.stark.miuix.data.dlna.soap.SoapClient
import com.stark.miuix.data.dlna.soap.SoapException
import com.stark.miuix.data.dlna.ssdp.SsdpDiscoveryFactory
import com.stark.miuix.util.AppLogger
import com.stark.miuix.util.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * DLNA 投屏状态
 *
 * 用于驱动 UI 显示：
 * - [Idle] 未投屏，可显示投屏按钮
 * - [Discovering] 正在搜索设备
 * - [Connecting] 正在投屏到设备
 * - [Casting] 投屏中，携带进度信息
 * - [Error] 投屏失败
 */
sealed interface DlnaState {
    /** 未投屏 */
    data object Idle : DlnaState
    /** 正在搜索设备 */
    data class Discovering(val devices: List<DlnaDevice>) : DlnaState
    /** 正在连接设备 */
    data class Connecting(val device: DlnaDevice) : DlnaState
    /** 投屏中 */
    data class Casting(
        val device: DlnaDevice,
        val positionMs: Long = 0L,
        val durationMs: Long = 0L,
        val isPlaying: Boolean = true
    ) : DlnaState
    /** 投屏失败 */
    data class Error(val message: String) : DlnaState
}

/**
 * DLNA 投屏控制器
 *
 * 业务编排核心，对外暴露 [state] StateFlow 供 UI 观察状态变化。
 *
 * 职责：
 * 1. 启动/停止设备发现
 * 2. 投屏到指定设备（SetAVTransportURI + Play）
 * 3. 投屏期间轮询进度（GetPositionInfo，1s 间隔）
 * 4. 控制命令转发：暂停/继续/停止/seek
 * 5. 设备掉线自动断开（连续 3 次轮询失败）
 *
 * 生命周期：
 * - 全局单例（在 AppContainer 中 lazy 创建）
 * - 进度轮询协程随投屏开始/结束启动/取消
 *
 * 错误处理：
 * - SOAP 失败：将状态置为 Error，UI 提示后用户可重试
 * - 轮询失败：累计 3 次后自动调用 stopCasting，回退到本地播放
 */
class DlnaController(
    networkClient: NetworkClient
) {
    private val soapClient = SoapClient(networkClient)
    private val ssdpFactory = SsdpDiscoveryFactory(networkClient)
    private val ssdpDiscovery = ssdpFactory.create()

    /** 当前平台是否支持投屏（UI 据此决定按钮可见性） */
    val isPlatformSupported: Boolean get() = ssdpFactory.isPlatformSupported

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var discoverJob: Job? = null
    private var progressJob: Job? = null

    private val _state = MutableStateFlow<DlnaState>(DlnaState.Idle)
    val state: StateFlow<DlnaState> = _state.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DlnaDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DlnaDevice>> = _discoveredDevices.asStateFlow()

    /**
     * 启动设备发现
     *
     * 在 5 秒窗口内持续推送发现的设备到 [discoveredDevices]。
     * 重复调用会先取消上一次发现任务。
     */
    fun startDiscovery() {
        if (!isPlatformSupported) {
            _state.value = DlnaState.Error("当前平台不支持投屏")
            return
        }
        discoverJob?.cancel()
        _discoveredDevices.value = emptyList()
        _state.value = DlnaState.Discovering(emptyList())

        discoverJob = scope.launch {
            try {
                ssdpDiscovery.discover().collectLatest { device ->
                    AppLogger.d("Dlna", "发现设备: ${device.friendlyName} (${device.udn})")
                    val current = _discoveredDevices.value
                    if (current.none { it.stableKey == device.stableKey }) {
                        _discoveredDevices.value = current + device
                        if (_state.value is DlnaState.Discovering) {
                            _state.value = DlnaState.Discovering(_discoveredDevices.value)
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("Dlna", "设备发现异常", e)
                _state.value = DlnaState.Error("设备搜索失败: ${e.message}")
            }
        }
    }

    /** 停止设备发现 */
    fun stopDiscovery() {
        discoverJob?.cancel()
        discoverJob = null
        if (_state.value is DlnaState.Discovering) {
            _state.value = DlnaState.Idle
        }
    }

    /**
     * 投屏到指定设备
     *
     * @param device 目标设备
     * @param mediaUrl 媒体 URL（必须是设备可直接访问的局域网 URL）
     * @param title 媒体标题
     * @param startPositionMs 起始位置（毫秒），>0 时投屏后自动 seek
     */
    fun cast(device: DlnaDevice, mediaUrl: String, title: String, startPositionMs: Long = 0L) {
        scope.launch {
            _state.value = DlnaState.Connecting(device)
            try {
                soapClient.cast(device, mediaUrl, title)
                if (startPositionMs > 0) {
                    runCatching { soapClient.seek(device, startPositionMs / 1000) }
                }
                _state.value = DlnaState.Casting(device = device, positionMs = startPositionMs)
                startProgressPolling(device)
                AppLogger.d("Dlna", "投屏成功: ${device.friendlyName}")
            } catch (e: SoapException) {
                AppLogger.e("Dlna", "投屏 SOAP 错误: ${e.code} ${e.description}", e)
                _state.value = DlnaState.Error("投屏失败: ${e.description}")
            } catch (e: Exception) {
                AppLogger.e("Dlna", "投屏异常", e)
                _state.value = DlnaState.Error("投屏失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    /** 暂停投屏播放 */
    fun pause() {
        val device = (_state.value as? DlnaState.Casting)?.device ?: return
        scope.launch {
            runCatching { soapClient.pause(device) }
                .onFailure { AppLogger.e("Dlna", "暂停失败", it) }
            updateCastingState { it.copy(isPlaying = false) }
        }
    }

    /** 继续投屏播放 */
    fun resume() {
        val device = (_state.value as? DlnaState.Casting)?.device ?: return
        scope.launch {
            runCatching { soapClient.resume(device) }
                .onFailure { AppLogger.e("Dlna", "继续播放失败", it) }
            updateCastingState { it.copy(isPlaying = true) }
        }
    }

    /** 跳转进度 */
    fun seekTo(positionMs: Long) {
        val device = (_state.value as? DlnaState.Casting)?.device ?: return
        scope.launch {
            runCatching { soapClient.seek(device, positionMs / 1000) }
                .onFailure { AppLogger.e("Dlna", "seek 失败", it) }
            updateCastingState { it.copy(positionMs = positionMs) }
        }
    }

    /**
     * 停止投屏
     *
     * 取消进度轮询 + 发送 Stop action + 重置状态。
     * 即使设备不可达也会重置本地状态，避免卡在 Casting。
     */
    fun stopCasting() {
        val device = (_state.value as? DlnaState.Casting)?.device
        progressJob?.cancel()
        progressJob = null
        if (device != null) {
            scope.launch {
                runCatching { soapClient.stop(device) }
                    .onFailure { AppLogger.d("Dlna", "停止投屏失败（设备可能已离线）: ${it.message}") }
            }
        }
        _state.value = DlnaState.Idle
        AppLogger.d("Dlna", "投屏已断开")
    }

    /**
     * 启动进度轮询协程
     *
     * 每秒调用 GetPositionInfo 更新状态，连续 3 次失败自动 stopCasting。
     */
    private fun startProgressPolling(device: DlnaDevice) {
        progressJob?.cancel()
        progressJob = scope.launch {
            var failCount = 0
            while (true) {
                delay(1000)
                if (_state.value !is DlnaState.Casting) break
                try {
                    val info: PositionInfo = soapClient.getPositionInfo(device)
                    val transportState = runCatching { soapClient.getTransportState(device) }
                        .getOrDefault("")
                    failCount = 0
                    updateCastingState { current ->
                        current.copy(
                            positionMs = if (info.positionMs >= 0) info.positionMs else current.positionMs,
                            durationMs = if (info.durationMs >= 0) info.durationMs else current.durationMs,
                            isPlaying = transportState != "PAUSED_PLAYBACK" && transportState != "STOPPED"
                        )
                    }
                } catch (e: Exception) {
                    failCount++
                    AppLogger.d("Dlna", "进度轮询失败 ($failCount): ${e.message}")
                    if (failCount >= MAX_POLL_FAILS) {
                        AppLogger.e("Dlna", "设备连续 $MAX_POLL_FAILS 次无响应，自动断开投屏", e)
                        stopCasting()
                        break
                    }
                }
            }
        }
    }

    /** 更新 Casting 状态（仅在当前为 Casting 时生效） */
    private fun updateCastingState(transform: (DlnaState.Casting) -> DlnaState.Casting) {
        val current = _state.value
        if (current is DlnaState.Casting) {
            _state.value = transform(current)
        }
    }

    companion object {
        private const val MAX_POLL_FAILS = 3
    }
}
