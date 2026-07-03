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
import com.stark.miuix.ui.theme.DesignTokens

/**
 * 视频网格 — 自适应列数
 *
 * 手机竖屏 2 列，横屏/平板自动增列（最小列宽 160dp）。
 */
@Composable
fun VideoGrid(
    videos: List<SearchResult>,
    onVideoClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = DesignTokens.gridMinWidth),
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = DesignTokens.screenPadding,
            vertical = DesignTokens.spacingSm
        ),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.cardGap),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.cardGap)
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
