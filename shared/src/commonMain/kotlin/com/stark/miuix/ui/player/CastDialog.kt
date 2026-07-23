/*
 * Copyright 2024 Stark Industries
 *
 * 投屏设备选择对话框
 *
 * Bottom Sheet 风格，调用 DlnaController 启动设备发现，
 * 实时显示已发现的设备列表，用户选中后回调到调用方。
 */
package com.stark.miuix.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.miuix.data.dlna.DlnaController
import com.stark.miuix.data.dlna.DlnaState
import com.stark.miuix.data.dlna.model.DlnaDevice
import com.stark.miuix.ui.icons.IconCast
import com.stark.miuix.theme.AppColors
import com.stark.miuix.ui.theme.DesignTokens
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 投屏对话框
 *
 * @param visible 是否显示
 * @param dlnaController DLNA 控制器
 * @param onDismiss 关闭回调
 * @param onSelectDevice 选中设备回调
 */
@Composable
fun CastDialog(
    visible: Boolean,
    dlnaController: DlnaController,
    onDismiss: () -> Unit,
    onSelectDevice: (DlnaDevice) -> Unit
) {
    val devices by dlnaController.discoveredDevices.collectAsState()
    val state by dlnaController.state.collectAsState()

    // 显示时启动发现，关闭时停止发现
    LaunchedEffect(visible) {
        if (visible) dlnaController.startDiscovery() else dlnaController.stopDiscovery()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 遮罩
            Box(modifier = Modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss))

            val density = LocalDensity.current
            Box(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .heightIn(max = 480.dp)
                    .clip(RoundedCornerShape(topStart = DesignTokens.radiusXl, topEnd = DesignTokens.radiusXl))
                    .background(MiuixTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 头部
                    Column(modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingMd)) {
                        // 顶部小拖把
                        Box(modifier = Modifier.size(32.dp, 4.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.25f)))
                        Spacer(modifier = Modifier.height(DesignTokens.spacingSm))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(text = "选择投屏设备",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f))
                            Text(text = "取消",
                                style = MiuixTheme.textStyles.footnote1,
                                color = AppColors.brand(),
                                modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
                        }
                    }

                    // 状态行
                    val stateText = when (state) {
                        is DlnaState.Discovering -> "正在搜索附近设备…"
                        is DlnaState.Error -> "搜索失败：${(state as DlnaState.Error).message}"
                        else -> if (devices.isEmpty()) "暂无可用设备" else "已发现 ${devices.size} 个设备"
                    }
                    Row(modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = DesignTokens.screenPadding, vertical = DesignTokens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (state is DlnaState.Discovering) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text(text = stateText,
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.spacingSm))

                    // 设备列表
                    LazyColumn(modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = DesignTokens.screenPadding,
                            vertical = DesignTokens.spacingSm
                        ),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.spacingSm)) {
                        items(devices, key = { it.stableKey }) { device ->
                            DeviceItem(device = device, onClick = { onSelectDevice(device) })
                        }
                    }
                    Spacer(modifier = Modifier.height(DesignTokens.spacingLg))
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: DlnaDevice, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(DesignTokens.radiusMd))
        .background(MiuixTheme.colorScheme.surfaceVariant)
        .clickable(onClick = onClick)
        .padding(horizontal = DesignTokens.spacingMd, vertical = DesignTokens.spacingMd),
        verticalAlignment = Alignment.CenterVertically) {
        Image(painter = rememberVectorPainter(IconCast),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurface),
            modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = device.friendlyName,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (device.manufacturer.isNotBlank() || device.modelName.isNotBlank()) {
                val subtitle = listOf(device.manufacturer, device.modelName)
                    .filter { it.isNotBlank() }.joinToString(" · ")
                Text(text = subtitle,
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1)
            }
        }
    }
}
