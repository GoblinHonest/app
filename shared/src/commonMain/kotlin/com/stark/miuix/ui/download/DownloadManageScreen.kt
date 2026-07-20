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

package com.stark.miuix.ui.download

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.download.DownloadManager
import com.stark.miuix.data.model.DownloadStatus
import com.stark.miuix.data.model.DownloadTask
import com.stark.miuix.ui.theme.DesignTokens
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 下载管理页
 *
 * 显示所有下载任务，支持暂停/继续/删除操作。
 */
@Composable
fun DownloadManageScreen(
    downloadManager: DownloadManager,
    onNavigateBack: () -> Unit
) {
    val tasks by downloadManager.tasks.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Text("<", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("下载管理", style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface)
        }

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无下载任务", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = DesignTokens.screenPadding,
                    vertical = DesignTokens.spacingSm
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
            ) {
                items(tasks, key = { it.id }) { task ->
                    DownloadTaskCard(
                        task = task,
                        onPause = { downloadManager.pauseTask(task.id) },
                        onResume = { downloadManager.resumeTask(task.id) },
                        onCancel = { downloadManager.removeTask(task.id) },
                        onRetry = { downloadManager.resumeTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadTaskCard(
    task: DownloadTask,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(MiuixTheme.colorScheme.surface)
            .padding(DesignTokens.spacingMd)
    ) {
        Text(
            text = task.title,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = task.episodeName.ifBlank { task.title },
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(MiuixTheme.colorScheme.surfaceVariant))
        Box(modifier = Modifier.fillMaxWidth(task.progress / 100f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(DesignTokens.brandBlue))

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (task.status) {
                    DownloadStatus.PENDING -> "等待中"
                    DownloadStatus.DOWNLOADING -> "${(task.progress * 100).toInt()}%"
                    DownloadStatus.PAUSED -> "已暂停"
                    DownloadStatus.COMPLETED -> "已完成"
                    DownloadStatus.FAILED -> "失败"
                },
                style = MiuixTheme.textStyles.footnote2,
                color = when (task.status) {
                    DownloadStatus.COMPLETED -> DesignTokens.brandBlue
                    DownloadStatus.FAILED -> MiuixTheme.colorScheme.error
                    else -> MiuixTheme.colorScheme.onSurfaceVariantSummary
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (task.status) {
                    DownloadStatus.DOWNLOADING -> ActionButton("暂停", onPause)
                    DownloadStatus.PAUSED -> {
                        ActionButton("继续", onResume)
                        ActionButton("取消", onCancel)
                    }
                    DownloadStatus.FAILED -> {
                        ActionButton("重试", onRetry)
                        ActionButton("取消", onCancel)
                    }
                    DownloadStatus.PENDING -> ActionButton("取消", onCancel)
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurface)
    }
}
