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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.ui.layout.adaptiveGridColumns
import com.stark.miuix.ui.layout.adaptiveScreenPadding
import com.stark.miuix.ui.theme.DesignTokens

/** 视频网格 — 响应式列数（Compact 3列 / Medium 4列 / Expanded 5列 / Large 6列） */
@Composable
fun VideoGrid(
    videos: List<SearchResult>,
    onVideoClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = adaptiveGridColumns()
    val padding = adaptiveScreenPadding()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = padding,
            vertical = DesignTokens.spacingSm
        ),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingMd)
    ) {
        items(
            items = videos,
            key = { "${it.sourceName}:${it.url}" }
        ) { video ->
            VideoCard(
                searchResult = video,
                onClick = { onVideoClick(video) }
            )
        }
    }
}
