/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.settings

import com.stark.miuix.data.storage.LocalStorage
import com.stark.miuix.data.sync.SyncManager
import com.stark.miuix.theme.ThemeMode
import com.stark.miuix.theme.ThemeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val autoPlayNext: Boolean = true,
    val backgroundPlay: Boolean = false,
    val cacheSize: String = "0 KB",
    val backupMessage: String = "",
    val syncMessage: String = ""
)

class SettingsViewModel(
    private val localStorage: LocalStorage? = null,
    private val syncManager: SyncManager? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _local = MutableStateFlow(
        SettingsState(themeMode = ThemeState.themeMode.value)
    )

    val uiState: StateFlow<SettingsState> = combine(
        ThemeState.themeMode,
        _local
    ) { themeMode, local ->
        local.copy(themeMode = themeMode)
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        SettingsState(themeMode = ThemeState.themeMode.value)
    )

    fun setThemeMode(mode: ThemeMode) {
        ThemeState.setThemeMode(mode)
    }

    fun setAutoPlayNext(enabled: Boolean) {
        _local.value = _local.value.copy(autoPlayNext = enabled)
    }

    fun setBackgroundPlay(enabled: Boolean) {
        _local.value = _local.value.copy(backgroundPlay = enabled)
    }

    fun clearCache() {
        scope.launch {
            _local.value = _local.value.copy(cacheSize = "0 KB")
        }
    }

    fun exportBackup(): String? {
        return localStorage?.exportAllData()
    }

    fun importBackup(json: String) {
        val storage = localStorage ?: return
        scope.launch {
            val result = storage.importAllData(json)
            result.fold(
                onSuccess = { count ->
                    _local.value = _local.value.copy(backupMessage = "恢复成功，共 $count 条数据")
                },
                onFailure = { e ->
                    _local.value = _local.value.copy(backupMessage = "恢复失败: ${e.message}")
                }
            )
        }
    }

    fun configureSync(url: String, username: String, password: String) {
        syncManager?.configure(url, username, password)
    }

    fun syncUpload() {
        val manager = syncManager ?: return
        scope.launch {
            _local.value = _local.value.copy(syncMessage = "正在上传...")
            manager.upload().fold(
                onSuccess = {
                    _local.value = _local.value.copy(syncMessage = "上传成功")
                },
                onFailure = { e ->
                    _local.value = _local.value.copy(syncMessage = "上传失败: ${e.message}")
                }
            )
        }
    }

    fun syncDownload() {
        val manager = syncManager ?: return
        scope.launch {
            _local.value = _local.value.copy(syncMessage = "正在下载...")
            manager.download().fold(
                onSuccess = { count ->
                    _local.value = _local.value.copy(syncMessage = "同步成功，恢复 $count 条")
                },
                onFailure = { e ->
                    _local.value = _local.value.copy(syncMessage = "同步失败: ${e.message}")
                }
            )
        }
    }

    fun clearMessage() {
        _local.value = _local.value.copy(backupMessage = "", syncMessage = "")
    }
}
