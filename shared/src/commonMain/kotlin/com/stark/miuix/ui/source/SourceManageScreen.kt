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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.stark.miuix.data.repository.SourceRepository
import com.stark.miuix.ui.components.SourceCard
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.CoroutineScope

/**
 * 视频源管理页
 *
 * 功能：展示已导入的视频源列表、启用/禁用切换、导入 JSON 格式视频源。
 *
 * @param sourceRepository 视频源仓库
 * @param onNavigateBack 返回上一页
 */
@Composable
fun SourceManageScreen(
    sourceRepository: SourceRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = SourceManageViewModel(sourceRepository, scope)
    val sources by viewModel.sources.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var importText by remember { mutableStateOf("") }
    var showImport by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "视频源管理",
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Text("返回", style = MiuixTheme.textStyles.body2)
                }
            },
            actions = {
                IconButton(onClick = { showImport = !showImport }) {
                    Text("导入", style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary)
                }
            }
        )

        // 导入区域
        if (showImport) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                TextField(
                    value = importText,
                    onValueChange = { importText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "粘贴视频源 JSON..."
                )
                Text(
                    text = "点击导入",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // 导入状态提示
        when (val state = uiState) {
            is SourceManageUiState.ImportSuccess -> {
                Text(
                    text = "成功导入 ${state.count} 个视频源",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is SourceManageUiState.ImportError -> {
                Text(
                    text = state.message,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {}
        }

        // 视频源列表
        if (sources.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "暂无视频源，请导入",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sources, key = { it.sourceName }) { source ->
                    SourceCard(
                        source = source,
                        onToggle = { viewModel.toggleSource(source.sourceName) },
                        onDelete = { viewModel.removeSource(source.sourceName) }
                    )
                }
            }
        }
    }
}
