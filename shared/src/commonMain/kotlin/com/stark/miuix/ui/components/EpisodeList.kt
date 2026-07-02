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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.Episode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 剧集列表组件 — 流式标签布局
 *
 * 使用 [FlowRow] 以标签云形式紧凑展示所有剧集，
 * 适合集数较多的番剧/电视剧，比逐行卡片更节省空间。
 *
 * @param episodes 剧集列表
 * @param onEpisodeClick 剧集点击回调
 * @param modifier Modifier 修饰符
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EpisodeList(
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        episodes.forEachIndexed { index, episode ->
            Card(
                modifier = Modifier.clickable { onEpisodeClick(episode) },
                cornerRadius = 10.dp
            ) {
                Text(
                    text = episode.name.ifBlank { "第 ${index + 1} 集" },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    }
}
