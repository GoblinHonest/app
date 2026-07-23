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
import kotlinx.coroutines.launch
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.source.SourceRepoItem
import com.stark.miuix.data.source.SourceRepoManager
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.theme.DesignTokens
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 在线源仓库页
 *
 * 显示可用源仓库列表，支持一键导入和订阅。
 */
@Composable
fun SourceRepoScreen(
    sourceRepoManager: SourceRepoManager,
    onNavigateBack: () -> Unit
) {
    val repos by sourceRepoManager.repos.collectAsState()
    val isLoading by sourceRepoManager.isLoading.collectAsState()
    val message by sourceRepoManager.message.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

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
            Text("源仓库", style = MiuixTheme.textStyles.title3, color = MiuixTheme.colorScheme.onSurface)
        }

        if (message.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.screenPadding)
                    .clip(RoundedCornerShape(DesignTokens.radiusMd))
                    .background(MiuixTheme.colorScheme.surfaceVariant)
                    .padding(DesignTokens.spacingMd)
            ) {
                Text(message, style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = DesignTokens.screenPadding,
                vertical = DesignTokens.spacingSm
            ),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)
        ) {
            items(repos) { repo ->
                RepoCard(
                    repo = repo,
                    isLoading = isLoading,
                    onImport = {
                        scope.launch {
                            sourceRepoManager.importFromRepo(repo.url)
                        }
                    },
                    onToggleSubscribe = { sourceRepoManager.toggleSubscribe(repo.url) }
                )
            }
        }
    }
}

@Composable
private fun RepoCard(
    repo: SourceRepoItem,
    isLoading: Boolean,
    onImport: () -> Unit,
    onToggleSubscribe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .background(MiuixTheme.colorScheme.surface)
            .padding(DesignTokens.spacingMd)
    ) {
        Text(repo.name, style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
        if (repo.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                repo.description,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.brand())
                    .clickable(enabled = !isLoading, onClick = onImport)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isLoading) "导入中..." else "一键导入",
                    style = MiuixTheme.textStyles.footnote2,
                    color = AppColors.onBrand()
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (repo.subscribed) AppColors.brand().copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onToggleSubscribe)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    if (repo.subscribed) "已订阅" else "订阅",
                    style = MiuixTheme.textStyles.footnote2,
                    color = if (repo.subscribed) AppColors.brand() else MiuixTheme.colorScheme.onSurface
                )
            }
        }
    }
}
