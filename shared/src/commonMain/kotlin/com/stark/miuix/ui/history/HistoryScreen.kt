/*
 * Copyright 2024 Stark Industries
 *
 * 观看历史页 — 时间分组列表，支持续播与清空
 */
package com.stark.miuix.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.stark.miuix.data.model.WatchHistory
import com.stark.miuix.data.model.WatchProgress
import com.stark.miuix.data.repository.UserDataRepository
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.icons.IconBack
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.TimeUtils
import com.stark.miuix.util.currentTimeMillis
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HistoryScreen(
    userDataRepository: UserDataRepository,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String, String, String, String) -> Unit
) {
    val history by userDataRepository.watchHistory.collectAsState()
    val progressList by userDataRepository.progressList.collectAsState()
    val progressMap = remember(progressList) {
        progressList.associateBy { it.videoId }
    }
    val groups = remember(history) { groupHistory(history) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background)
    ) {
        HistoryTopBar(
            count = history.size,
            onBack = onNavigateBack,
            onClear = {
                if (history.isNotEmpty()) {
                    userDataRepository.clearHistory()
                    userDataRepository.clearProgress()
                }
            },
            canClear = history.isNotEmpty()
        )

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "暂无观看记录",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "去首页挑一部片子开看吧",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = DesignTokens.screenPadding,
                    end = DesignTokens.screenPadding,
                    top = DesignTokens.spacingSm,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
            ) {
                groups.forEach { group ->
                    item(key = "header-${group.label}") {
                        Text(
                            text = group.label,
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.outline,
                            modifier = Modifier.padding(
                                top = DesignTokens.spacingMd,
                                bottom = DesignTokens.spacingXs
                            )
                        )
                    }
                    items(group.items, key = { it.videoId + it.timestamp }) { item ->
                        HistoryCard(
                            item = item,
                            progress = progressMap[item.videoId],
                            onClick = {
                                onNavigateToDetail(
                                    item.sourceName,
                                    item.detailUrl,
                                    item.title,
                                    item.cover
                                )
                            },
                            onRemove = {
                                userDataRepository.removeHistory(item.videoId)
                                userDataRepository.removeProgress(item.videoId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTopBar(
    count: Int,
    onBack: () -> Unit,
    onClear: () -> Unit,
    canClear: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.brand().copy(alpha = 0.16f),
                        MiuixTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Image(
                painter = rememberVectorPainter(IconBack),
                contentDescription = "返回",
                colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
            Text(
                text = "观看历史",
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface
            )
            if (count > 0) {
                Text(
                    text = "共 $count 部",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline
                )
            }
        }
        if (canClear) {
            Text(
                text = "清空",
                style = MiuixTheme.textStyles.body2,
                color = AppColors.brand(),
                modifier = Modifier
                    .clickable(onClick = onClear)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun HistoryCard(
    item: WatchHistory,
    progress: WatchProgress?,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val fraction = progress?.progressFraction
        ?: item.progress.coerceIn(0f, 1f)
    val episodeText = when {
        progress != null && progress.episodeName.isNotBlank() -> progress.episodeName
        item.lastEpisode.isNotBlank() -> item.lastEpisode
        else -> ""
    }
    val timeText = if (item.timestamp > 0) {
        TimeUtils.formatRelative(item.timestamp)
    } else {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.radiusLg))
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(DesignTokens.spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 封面 + 进度条
        Box(
            modifier = Modifier
                .width(86.dp)
                .height(114.dp)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceContainerHighest)
        ) {
            if (item.cover.isNotBlank() &&
                (item.cover.startsWith("http://") || item.cover.startsWith("https://"))
            ) {
                val ctx = LocalPlatformContext.current
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(item.cover).build(),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = item.title.take(1),
                        style = MiuixTheme.textStyles.headline1,
                        color = AppColors.brand()
                    )
                }
            }
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(3.dp)
                            .background(AppColors.brand())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(DesignTokens.spacingMd))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            val meta = buildList {
                if (item.sourceName.isNotBlank()) add(item.sourceName)
                if (episodeText.isNotBlank()) add(episodeText)
            }.joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (timeText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeText,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "继续观看",
                    style = MiuixTheme.textStyles.footnote1,
                    color = AppColors.onBrand(),
                    modifier = Modifier
                        .clip(RoundedCornerShape(DesignTokens.radiusPill))
                        .background(AppColors.brand())
                        .clickable(onClick = onClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.spacingMd))
                Text(
                    text = "删除",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.outline,
                    modifier = Modifier
                        .clickable(onClick = onRemove)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private data class HistoryGroup(
    val label: String,
    val items: List<WatchHistory>
)

private fun groupHistory(list: List<WatchHistory>): List<HistoryGroup> {
    if (list.isEmpty()) return emptyList()
    val now = currentTimeMillis()
    val dayMs = 24L * 60 * 60 * 1000
    val startOfToday = now - (now % dayMs)
    // 简化：用相对天数分组
    val buckets = linkedMapOf(
        "今天" to mutableListOf<WatchHistory>(),
        "昨天" to mutableListOf(),
        "近 7 天" to mutableListOf(),
        "更早" to mutableListOf()
    )
    list.forEach { item ->
        val t = item.timestamp
        val key = when {
            t <= 0L -> "更早"
            t >= startOfToday -> "今天"
            t >= startOfToday - dayMs -> "昨天"
            t >= startOfToday - 7 * dayMs -> "近 7 天"
            else -> "更早"
        }
        buckets.getValue(key).add(item)
    }
    return buckets
        .filter { it.value.isNotEmpty() }
        .map { HistoryGroup(it.key, it.value) }
}
