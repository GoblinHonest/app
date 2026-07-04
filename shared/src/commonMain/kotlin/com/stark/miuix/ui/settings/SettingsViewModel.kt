/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.settings

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
    val cacheSize: String = "0 KB"
)

/** 亮色/暗色 */
enum class ThemeMode(val label: String) {
    LIGHT("亮色模式"),
    DARK("暗色模式")
}

class SettingsViewModel {
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
}
