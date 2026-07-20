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

package com.stark.miuix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.StringUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频源卡片组件
 *
 * 展示源的完整信息（名称、域名、分组、版本、备注），
 * 提供启用/禁用开关和带确认的删除操作。
 *
 * @param source 视频源数据
 * @param onToggle 切换启用状态回调
 * @param onDelete 确认删除回调
 */
@Composable
fun SourceCard(
    source: VideoSource,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        cornerRadius = DesignTokens.radiusCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.sourceName,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Text(
                        text = StringUtils.extractDomain(source.sourceUrl),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = source.enabled,
                    onCheckedChange = { onToggle() }
                )
            }

            // 附加信息行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (source.sourceGroup.isNotBlank()) {
                    Text(
                        text = source.sourceGroup,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "v${source.version}",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = if (source.sourceGroup.isNotBlank()) 8.dp else 0.dp)
                )
                if (source.lastCheckStatus.isNotBlank()) {
                    val statusColor = when (source.lastCheckStatus) {
                        "OK" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                        "TIMEOUT" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                        else -> MiuixTheme.colorScheme.error
                    }
                    val statusLabel = when (source.lastCheckStatus) {
                        "OK" -> "正常"
                        "TIMEOUT" -> "超时"
                        "PARSE_ERROR" -> "解析错误"
                        "UNREACHABLE" -> "不可达"
                        else -> source.lastCheckStatus
                    }
                    Text(
                        text = statusLabel,
                        style = MiuixTheme.textStyles.footnote2,
                        color = statusColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    if (source.lastCheckLatencyMs > 0) {
                        Text(
                            text = "${source.lastCheckLatencyMs}ms",
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                if (!showDeleteConfirm) {
                    Text(
                        text = "删除",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier
                            .clickable { showDeleteConfirm = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // 删除确认区
            if (showDeleteConfirm) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = DesignTokens.radiusCard
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "确认删除此视频源?",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "取消",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.outline,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { showDeleteConfirm = false }
                        )
                        Text(
                            text = "删除",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.error,
                            modifier = Modifier.clickable {
                                onDelete()
                                showDeleteConfirm = false
                            }
                        )
                    }
                }
            }

            // 备注
            if (source.sourceComment.isNotBlank()) {
                Text(
                    text = source.sourceComment,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 6.dp),
                    maxLines = 2
                )
            }
        }
    }
}
