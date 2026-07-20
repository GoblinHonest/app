package com.stark.miuix.ui.player

import androidx.compose.runtime.Composable
import com.stark.miuix.data.dlna.DlnaController

/**
 * 全屏播放器叠加控件（expect 声明）
 *
 * 各平台根据自身能力提供全屏手势控制 UI：
 * - Android: 完整手势（亮度/音量/进度/倍速/锁屏）+ 投屏
 * - 其他: 留空（使用内嵌控制条替代）
 *
 * @param dlnaController DLNA 投屏控制器，仅 Android 平台用于驱动投屏 UI；
 *                       其他平台传入 null 即可
 */
@Composable
expect fun FullscreenControls(
    url: String,
    title: String,
    onExitFullscreen: () -> Unit,
    isBuffering: Boolean = false,
    dlnaController: DlnaController? = null
)
