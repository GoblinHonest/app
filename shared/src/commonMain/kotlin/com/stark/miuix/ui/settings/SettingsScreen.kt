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

package com.stark.miuix.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.stark.miuix.util.AppLogger
import com.stark.miuix.util.ClipboardUtils

/**
 * 设置页
 *
 * HyperOS 风格分组卡片布局，包含：
 * - 主题配置（模式切换、动态取色）
 * - 播放器设置
 * - 存储管理（缓存清理）
 * - 关于信息
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel = remember { SettingsViewModel() }
    val settings by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "设置",
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("返回", style = MiuixTheme.textStyles.body2)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            // 主题设置
            SettingsSection(title = "外观") {
                ThemeMode.entries.forEach { mode ->
                    SettingsItem(
                        title = mode.label,
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 播放设置
            SettingsSection(title = "播放") {
                SettingsSwitchItem(
                    title = "自动播放下一集",
                    subtitle = "当前剧集结束后自动播放下一集",
                    checked = settings.autoPlayNext,
                    onCheckedChange = { viewModel.setAutoPlayNext(it) }
                )
                SettingsSwitchItem(
                    title = "后台播放",
                    subtitle = "切换到其他应用时继续播放音频",
                    checked = settings.backgroundPlay,
                    onCheckedChange = { viewModel.setBackgroundPlay(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 存储
            SettingsSection(title = "存储") {
                SettingsItem(
                    title = "缓存大小",
                    subtitle = settings.cacheSize,
                    actionText = "清除",
                    onAction = { viewModel.clearCache() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 调试
            SettingsSection(title = "调试") {
                val copyAction = ClipboardUtils.rememberCopyAction()
                val logs by AppLogger.logs.collectAsState()
                var showLogs by remember { mutableStateOf(false) }

                SettingsItem(
                    title = "应用日志",
                    subtitle = "最近 ${logs.size} 条 · 日志文件: ${AppLogger.getLogFile()}",
                    actionText = if (showLogs) "收起" else "查看",
                    onAction = { showLogs = !showLogs }
                )
                if (showLogs && logs.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        cornerRadius = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "复制全部",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        copyAction(logs.joinToString("\n"))
                                    }
                                )
                                Text(
                                    text = "清空",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.error,
                                    modifier = Modifier.clickable { AppLogger.clear() }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            logs.take(50).forEach { log ->
                                Text(
                                    text = log,
                                    style = MiuixTheme.textStyles.footnote2,
                                    color = if (log.contains("E/")) MiuixTheme.colorScheme.error
                                           else MiuixTheme.colorScheme.outline,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 关于
            SettingsSection(title = "关于") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    cornerRadius = 12.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Miuix 视频聚合",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v1.0.0 · Compose Multiplatform + Miuix",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/** 设置项分组标题 */
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.footnote1,
        color = MiuixTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    content()
}

/** 可选中的设置项 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String = "",
    selected: Boolean = false,
    actionText: String = "",
    onClick: () -> Unit = {},
    onAction: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.body1,
                    color = if (selected) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (actionText.isNotBlank()) {
                Text(
                    text = actionText,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onAction)
                )
            }
        }
    }
}

/** 带开关的设置项 */
@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
