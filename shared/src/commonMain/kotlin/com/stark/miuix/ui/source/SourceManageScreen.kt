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

package com.stark.miuix.ui.source

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.ui.components.SourceCard
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.stark.miuix.ui.icons.IconBack
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频源管理页
 *
 * 支持两种导入方式：
 * - JSON 粘贴：直接粘贴视频源 JSON 配置
 * - URL 订阅：输入远程订阅地址，自动拉取并解析
 */
@Composable
fun SourceManageScreen(
    sourceRepository: SourceRepository,
    onNavigateBack: () -> Unit
) {
    val viewModel = remember(sourceRepository) { SourceManageViewModel(sourceRepository) }
    val sources by viewModel.sources.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var importText by remember { mutableStateOf("") }
    var showImport by remember { mutableStateOf(false) }
    var importMode by remember { mutableStateOf(ImportMode.JSON) }

    // 导入成功后自动收起输入面板
    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is SourceManageUiState.ImportSuccess) {
            showImport = false
            importText = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "视频源管理",
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    androidx.compose.foundation.Image(painter=rememberVectorPainter(IconBack),contentDescription="返回",colorFilter=ColorFilter.tint(MiuixTheme.colorScheme.onSurface),modifier=androidx.compose.ui.Modifier.size(20.dp))
                }
            },
            actions = {
                IconButton(onClick = { showImport = !showImport }) {
                    Text(
                        if (showImport) "收起" else "导入",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
        ) {
            if (showImport) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 导入模式切换 — 选中态用蓝色填充，未选中透明
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ImportMode.entries.forEach { mode ->
                            val isSelected = importMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) DesignTokens.brandBlue
                                        else MiuixTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { importMode = mode }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MiuixTheme.textStyles.body2,
                                    color = if (isSelected) Color.White
                                           else MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = importMode.hint,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = importMode.placeholder
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 确认导入 — 实心主色调按钮
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DesignTokens.radiusXl))
                            .background(
                                if (importText.isNotBlank()) DesignTokens.brandBlue
                                else MiuixTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                if (importText.isNotBlank()) {
                                    when (importMode) {
                                        ImportMode.JSON -> viewModel.importSource(importText)
                                        ImportMode.URL -> viewModel.importFromUrl(importText)
                                    }
                                    importText = ""
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "确认导入",
                            style = MiuixTheme.textStyles.body1,
                            color = if (importText.isNotBlank()) Color.White
                                   else MiuixTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 状态提示
            when (val state = uiState) {
                is SourceManageUiState.Loading -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "正在导入...",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.outline
                        )
                    }
                }
                is SourceManageUiState.ImportSuccess -> {
                    Text(
                        text = "成功导入 ${state.count} 个视频源",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                is SourceManageUiState.ImportError -> {
                    Text(
                        text = "导入失败: ${state.message}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                else -> {}
            }

            // 视频源列表
            if (sources.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sources, key = { it.sourceName }) { source ->
                        SourceCard(
                            source = source,
                            onToggle = { viewModel.toggleSource(source.sourceName) },
                            onDelete = { viewModel.removeSource(source.sourceName) }
                        )
                    }
                }
            } else if (!showImport) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂无视频源",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Text(
                            text = "点击右上角「导入」添加",
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

/** 导入模式 */
private enum class ImportMode(val label: String, val hint: String, val placeholder: String) {
    JSON("JSON 粘贴", "粘贴视频源 JSON 配置（支持单个或数组）：", "粘贴 JSON..."),
    URL("URL 订阅", "输入视频源订阅地址，自动拉取配置：", "https://example.com/sources.json")
}
