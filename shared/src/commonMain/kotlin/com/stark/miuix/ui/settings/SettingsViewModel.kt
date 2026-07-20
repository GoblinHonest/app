/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.settings

import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.data.sync.SyncManager
import com.stark.miuix.theme.ThemeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val autoPlayNext: Boolean = true,
    val backgroundPlay: Boolean = false,
    val cacheSize: String = "0 KB",
    val backupMessage: String = "",
    val syncMessage: String = ""
)

/** 亮色/暗色 */
enum class ThemeMode(val label: String) {
    LIGHT("亮色模式"),
    DARK("暗色模式")
}

class SettingsViewModel(
    private val localStorage: LocalStorage? = null,
    private val syncManager: SyncManager? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        ThemeState.setThemeMode(mode)
    }

    fun setAutoPlayNext(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoPlayNext = enabled)
    }

    fun setBackgroundPlay(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(backgroundPlay = enabled)
    }

    fun clearCache() {
        scope.launch {
            _uiState.value = _uiState.value.copy(cacheSize = "0 KB")
        }
    }

    /** 导出备份数据为 JSON 字符串 */
    fun exportBackup(): String? {
        return localStorage?.exportAllData()
    }

    /** 从备份 JSON 恢复数据 */
    fun importBackup(json: String) {
        val storage = localStorage ?: return
        scope.launch {
            val result = storage.importAllData(json)
            result.fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(backupMessage = "恢复成功，共 $count 条数据")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(backupMessage = "恢复失败: ${e.message}")
                }
            )
        }
    }

    /** 配置 WebDAV 同步 */
    fun configureSync(url: String, username: String, password: String) {
        syncManager?.configure(url, username, password)
    }

    /** 上传数据到 WebDAV */
    fun syncUpload() {
        val manager = syncManager ?: return
        scope.launch {
            _uiState.value = _uiState.value.copy(syncMessage = "正在上传...")
            manager.upload().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(syncMessage = "上传成功") },
                onFailure = { e -> _uiState.value = _uiState.value.copy(syncMessage = "上传失败: ${e.message}") }
            )
        }
    }

    /** 从 WebDAV 下载并恢复 */
    fun syncDownload() {
        val manager = syncManager ?: return
        scope.launch {
            _uiState.value = _uiState.value.copy(syncMessage = "正在下载...")
            manager.download().fold(
                onSuccess = { count -> _uiState.value = _uiState.value.copy(syncMessage = "同步成功，恢复 $count 条") },
                onFailure = { e -> _uiState.value = _uiState.value.copy(syncMessage = "同步失败: ${e.message}") }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(backupMessage = "", syncMessage = "")
    }
}
