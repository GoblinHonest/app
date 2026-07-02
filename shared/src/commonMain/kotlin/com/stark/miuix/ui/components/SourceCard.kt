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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.VideoSource
import com.stark.miuix.util.StringUtils
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视频源卡片组件
 *
 * 展示视频源的详细信息，包括名称、域名、分组和启用/禁用开关。
 *
 * @param source 视频源数据
 * @param onToggle 切换启用状态的回调
 * @param onDelete 删除视频源的回调
 * @param modifier Modifier 修饰符
 */
@Composable
fun SourceCard(
    source: VideoSource,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            // 源信息
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

                if (source.sourceGroup.isNotBlank()) {
                    Text(
                        text = source.sourceGroup,
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // 启用/禁用开关
            Switch(
                checked = source.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
