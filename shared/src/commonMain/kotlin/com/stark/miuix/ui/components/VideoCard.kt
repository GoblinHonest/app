/*
 * Copyright 2024 Stark Industries
 */

package com.stark.miuix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.model.SearchResult
import com.stark.miuix.ui.theme.DesignTokens
import com.stark.miuix.util.AppLogger
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun VideoCard(
    searchResult: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.radiusMd))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(DesignTokens.coverAspectRatio)
                .clip(RoundedCornerShape(DesignTokens.radiusMd))
                .background(MiuixTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val coverUrl = searchResult.cover
            if (coverUrl.isNotBlank() && (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))) {
                val painterResource = asyncPainterResource(data = coverUrl)
                KamelImage(
                    resource = painterResource,
                    contentDescription = searchResult.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { progress ->
                        Box(
                            modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "...",
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.outline
                            )
                        }
                    },
                    onFailure = { exception ->
                        AppLogger.e("Image", "加载失败: $coverUrl", exception)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = searchResult.title.take(2),
                                style = MiuixTheme.textStyles.headline1,
                                color = MiuixTheme.colorScheme.outline
                            )
                        }
                    }
                )
            } else {
                Text(
                    text = searchResult.title.take(2),
                    style = MiuixTheme.textStyles.headline1,
                    color = MiuixTheme.colorScheme.outline
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
        }

        Column(modifier = Modifier.padding(horizontal = DesignTokens.spacingXs, vertical = DesignTokens.spacingSm)) {
            Text(
                text = searchResult.title,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (searchResult.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(DesignTokens.spacingXs))
                Text(
                    text = searchResult.description,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
