/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.stark.miuix.di.AppContainer
import com.stark.miuix.ui.components.EmptyStateView
import com.stark.miuix.ui.components.ErrorStateView
import com.stark.miuix.ui.components.ShimmerVideoGrid
import com.stark.miuix.ui.components.VideoGrid
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.icons.IconCancel
import com.stark.miuix.ui.icons.IconHistory
import com.stark.miuix.ui.icons.IconRank
import com.stark.miuix.ui.icons.IconSearch
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.theme.DesignTokens
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 搜索页 — 全屏搜索体验
 *
 * 结构：
 * 1. 顶部搜索栏（返回 + 输入框 + 清除 + 搜索按钮）
 * 2. 联想词下拉
 * 3. Idle：搜索历史 + 热搜榜
 * 4. Searching / Success / Error 结果态
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel = AppContainer.searchViewModel
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val hotSearches by viewModel.hotSearches.collectAsState()

    val initialQuery = when (val s = uiState) {
        is SearchUiState.Success -> s.keyword
        is SearchUiState.Error -> s.keyword
        else -> ""
    }
    var query by remember { mutableStateOf(initialQuery) }

    fun doSearch(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isBlank()) return
        query = trimmed
        viewModel.search(trimmed)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
    ) {
        SearchTopBar(
            query = query,
            onQueryChange = {
                query = it
                viewModel.onQueryChanged(it)
            },
            onSearch = { doSearch(query) },
            onClear = {
                query = ""
                viewModel.resetToIdle()
            },
            onBack = onNavigateBack
        )

        // 联想词下拉
        if (suggestions.isNotEmpty() && uiState is SearchUiState.Idle && query.isNotBlank()) {
            SuggestionList(
                suggestions = suggestions,
                onSelect = { doSearch(it) }
            )
        }

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                IdleContent(
                    history = history,
                    hotSearches = hotSearches,
                    onSelectKeyword = { doSearch(it) },
                    onRemoveHistory = { viewModel.removeHistoryItem(it) },
                    onClearHistory = { viewModel.clearHistory() }
                )
            }

            is SearchUiState.Searching -> {
                ShimmerVideoGrid()
            }

            is SearchUiState.Success -> {
                if (state.results.isEmpty()) {
                    EmptyStateView(
                        title = "未找到相关内容",
                        message = "试试换个关键词，或从热搜中选一个",
                        actionText = if (history.isNotEmpty()) "用「${history.first()}」重新搜" else "",
                        onAction = {
                            if (history.isNotEmpty()) doSearch(history.first())
                        }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val sourceCount = state.results.map { it.sourceName }.distinct().size
                        Text(
                            text = "从 ${sourceCount} 个源找到 ${state.results.size} 条结果",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.outline,
                            modifier = Modifier.padding(
                                horizontal = DesignTokens.screenPadding,
                                vertical = DesignTokens.spacingSm
                            )
                        )
                        VideoGrid(
                            videos = state.results,
                            onVideoClick = { video ->
                                onNavigateToDetail(
                                    video.sourceName, video.url, video.title, video.cover
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            is SearchUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { doSearch(query) }
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.brand().copy(alpha = 0.10f),
                        Color.Transparent
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = DesignTokens.screenPadding,
                    vertical = DesignTokens.spacingSm
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.touchTargetMin)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberVectorPainter(IconBack),
                    contentDescription = "返回",
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                    modifier = Modifier.size(DesignTokens.iconSizeMd)
                )
            }

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                label = "搜索视频、演员..."
            )

            if (query.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(DesignTokens.touchTargetMin)
                        .clip(CircleShape)
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberVectorPainter(IconCancel),
                        contentDescription = "清除",
                        colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                        modifier = Modifier.size(DesignTokens.iconSizeSm)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .heightIn(min = DesignTokens.touchTargetMin)
                    .clip(RoundedCornerShape(DesignTokens.radiusMd))
                    .background(AppColors.brand())
                    .clickable(onClick = onSearch)
                    .padding(horizontal = DesignTokens.spacingLg, vertical = DesignTokens.spacingSm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "搜索",
                    style = MiuixTheme.textStyles.body2,
                    color = AppColors.onBrand()
                )
            }
        }
    }
}

@Composable
private fun SuggestionList(
    suggestions: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.screenPadding)
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(MiuixTheme.colorScheme.surfaceVariant)
    ) {
        suggestions.forEachIndexed { index, suggestion ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(horizontal = DesignTokens.spacingMd)
                        .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.15f))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = DesignTokens.touchTargetMin)
                    .clickable { onSelect(suggestion) }
                    .padding(horizontal = DesignTokens.spacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberVectorPainter(IconSearch),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                    modifier = Modifier.size(DesignTokens.iconSizeSm)
                )
                Spacer(modifier = Modifier.width(DesignTokens.spacingSm))
                Text(
                    text = suggestion,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdleContent(
    history: List<String>,
    hotSearches: List<String>,
    onSelectKeyword: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.screenPadding)
            .padding(top = DesignTokens.spacingMd, bottom = DesignTokens.spacingXl)
    ) {
        if (history.isNotEmpty()) {
            SectionHeader(
                icon = IconHistory,
                title = "搜索历史",
                actionText = "清除",
                onAction = onClearHistory
            )
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
            ) {
                history.forEach { keyword ->
                    HistoryChip(
                        keyword = keyword,
                        onClick = { onSelectKeyword(keyword) },
                        onRemove = { onRemoveHistory(keyword) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(DesignTokens.spacingXl))
        }

        if (hotSearches.isNotEmpty()) {
            SectionHeader(
                icon = IconRank,
                title = "热搜榜",
                actionText = "",
                onAction = {}
            )
            Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
            hotSearches.forEachIndexed { index, keyword ->
                HotSearchRow(
                    rank = index + 1,
                    keyword = keyword,
                    onClick = { onSelectKeyword(keyword) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    actionText: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberVectorPainter(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(AppColors.brand()),
            modifier = Modifier.size(DesignTokens.iconSizeSm)
        )
        Spacer(modifier = Modifier.width(DesignTokens.spacingSm))
        Text(
            text = title,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (actionText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .heightIn(min = DesignTokens.touchTargetMin)
                    .clickable(onClick = onAction)
                    .padding(horizontal = DesignTokens.spacingSm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun HistoryChip(
    keyword: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .heightIn(min = DesignTokens.touchTargetMin)
            .clip(RoundedCornerShape(DesignTokens.radiusPill))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(start = DesignTokens.spacingMd, end = DesignTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = keyword,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .size(DesignTokens.touchTargetMin)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberVectorPainter(IconCancel),
                contentDescription = "删除",
                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.outline),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun HotSearchRow(
    rank: Int,
    keyword: String,
    onClick: () -> Unit
) {
    val rankColor = when (rank) {
        1 -> DesignTokens.badgeRed
        2 -> DesignTokens.badgeOrange
        3 -> DesignTokens.gold
        else -> MiuixTheme.colorScheme.outline
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DesignTokens.touchTargetMin)
            .clip(RoundedCornerShape(DesignTokens.radiusSm))
            .clickable(onClick = onClick)
            .padding(horizontal = DesignTokens.spacingXs, vertical = DesignTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MiuixTheme.textStyles.body2,
                color = rankColor
            )
        }
        Text(
            text = keyword,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (rank <= 3) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(DesignTokens.radiusXs))
                    .background(rankColor.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "热",
                    style = MiuixTheme.textStyles.footnote2,
                    color = rankColor
                )
            }
        }
    }
}
