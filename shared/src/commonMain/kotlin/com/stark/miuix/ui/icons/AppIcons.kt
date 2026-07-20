/*
 * Copyright 2024 Stark Industries
 *
 * CineHub 自绘图标系统 v2
 * 规格：24x24 viewBox，1.8px 线宽，Round cap/join
 * 使用：Image(painter = rememberVectorPainter(icon), colorFilter = ColorFilter.tint(color))
 */

package com.stark.miuix.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private const val VB = 24f
private const val SW = 1.8f

private fun icon(block: ImageVector.Builder.() -> Unit): ImageVector {
    val b = ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = VB, viewportHeight = VB)
    b.block()
    return b.build()
}

private fun ImageVector.Builder.sp(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = SW, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = block)
}

private fun ImageVector.Builder.fp(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) {
    path(fill = SolidColor(Color.Black), pathBuilder = block)
}

// ─── 导航 ───

val IconHome: ImageVector get() = icon { sp {
    moveTo(4f, 10.5f); lineTo(12f, 4f); lineTo(20f, 10.5f)
    moveTo(6f, 9.5f); lineTo(6f, 19f)
    arcTo(1f, 1f, 0f, false, false, 7f, 20f); lineTo(17f, 20f)
    arcTo(1f, 1f, 0f, false, false, 18f, 19f); lineTo(18f, 9.5f)
    moveTo(10f, 20f); lineTo(10f, 15f)
    arcTo(1f, 1f, 0f, false, true, 11f, 14f); lineTo(13f, 14f)
    arcTo(1f, 1f, 0f, false, true, 14f, 15f); lineTo(14f, 20f)
}}

val IconSearch: ImageVector get() = icon { sp {
    moveTo(16.5f, 11f)
    arcTo(5.5f, 5.5f, 0f, true, true, 11f, 16.5f)
    arcTo(5.5f, 5.5f, 0f, true, true, 5.5f, 11f)
    arcTo(5.5f, 5.5f, 0f, true, true, 16.5f, 11f)
    moveTo(15.2f, 15.2f); lineTo(20f, 20f)
}}

val IconUser: ImageVector get() = icon { sp {
    moveTo(15.5f, 7.5f)
    arcTo(3.5f, 3.5f, 0f, true, true, 12f, 11f)
    arcTo(3.5f, 3.5f, 0f, true, true, 8.5f, 7.5f)
    arcTo(3.5f, 3.5f, 0f, true, true, 15.5f, 7.5f)
    moveTo(4.5f, 20f)
    curveTo(4.5f, 17f, 7.8f, 14.5f, 12f, 14.5f)
    curveTo(16.2f, 14.5f, 19.5f, 17f, 19.5f, 20f)
}}

// ─── 播放器控制 ───

val IconPlay: ImageVector get() = icon { fp {
    moveTo(8f, 5.5f); lineTo(8f, 18.5f)
    arcTo(0.8f, 0.8f, 0f, false, false, 9.2f, 19.2f)
    lineTo(18.5f, 12.7f)
    arcTo(0.8f, 0.8f, 0f, false, false, 18.5f, 11.3f)
    lineTo(9.2f, 4.8f)
    arcTo(0.8f, 0.8f, 0f, false, false, 8f, 5.5f)
    close()
}}

val IconPause: ImageVector get() = icon { sp {
    moveTo(9f, 5.5f); lineTo(9f, 18.5f)
    moveTo(15f, 5.5f); lineTo(15f, 18.5f)
}}

val IconNext: ImageVector get() = icon { sp {
    moveTo(6f, 6f); lineTo(14f, 12f); lineTo(6f, 18f)
    moveTo(17.5f, 6f); lineTo(17.5f, 18f)
}}

val IconFullscreen: ImageVector get() = icon { sp {
    moveTo(4f, 9f); lineTo(4f, 5f); arcTo(1f, 1f, 0f, false, true, 5f, 4f); lineTo(9f, 4f)
    moveTo(15f, 4f); lineTo(19f, 4f); arcTo(1f, 1f, 0f, false, true, 20f, 5f); lineTo(20f, 9f)
    moveTo(20f, 15f); lineTo(20f, 19f); arcTo(1f, 1f, 0f, false, true, 19f, 20f); lineTo(15f, 20f)
    moveTo(9f, 20f); lineTo(5f, 20f); arcTo(1f, 1f, 0f, false, true, 4f, 19f); lineTo(4f, 15f)
}}

val IconExitFullscreen: ImageVector get() = icon { sp {
    moveTo(9f, 4f); lineTo(9f, 9f); lineTo(4f, 9f)
    moveTo(15f, 4f); lineTo(15f, 9f); lineTo(20f, 9f)
    moveTo(20f, 15f); lineTo(15f, 15f); lineTo(15f, 20f)
    moveTo(4f, 15f); lineTo(9f, 15f); lineTo(9f, 20f)
}}

val IconLock: ImageVector get() = icon { sp {
    moveTo(7f, 11f); lineTo(7f, 19f); arcTo(1f, 1f, 0f, false, false, 8f, 20f)
    lineTo(16f, 20f); arcTo(1f, 1f, 0f, false, false, 17f, 19f); lineTo(17f, 11f)
    arcTo(1f, 1f, 0f, false, false, 16f, 10f); lineTo(8f, 10f); arcTo(1f, 1f, 0f, false, false, 7f, 11f)
    moveTo(9f, 10f); lineTo(9f, 7f)
    arcTo(3f, 3f, 0f, false, true, 15f, 7f); lineTo(15f, 10f)
    moveTo(12f, 14f); lineTo(12f, 16f)
}}

// ─── 弹幕 ───

val IconDanmaku: ImageVector get() = icon { sp {
    moveTo(4f, 7.5f); lineTo(20f, 7.5f)
    moveTo(4f, 12f); lineTo(15f, 12f)
    moveTo(4f, 16.5f); lineTo(18f, 16.5f)
}}

val IconDanmakuOff: ImageVector get() = icon { sp {
    moveTo(4f, 7.5f); lineTo(20f, 7.5f)
    moveTo(4f, 12f); lineTo(15f, 12f)
    moveTo(4f, 16.5f); lineTo(12f, 16.5f)
    moveTo(16f, 14.5f); lineTo(20f, 18.5f)
    moveTo(20f, 14.5f); lineTo(16f, 18.5f)
}}

// ─── 操作 ───

val IconDownload: ImageVector get() = icon { sp {
    moveTo(12f, 4f); lineTo(12f, 14f)
    moveTo(8f, 10.5f); lineTo(12f, 14.5f); lineTo(16f, 10.5f)
    moveTo(4f, 17f); lineTo(4f, 19f); arcTo(1f, 1f, 0f, false, false, 5f, 20f)
    lineTo(19f, 20f); arcTo(1f, 1f, 0f, false, false, 20f, 19f); lineTo(20f, 17f)
}}

val IconShare: ImageVector get() = icon { sp {
    moveTo(8.5f, 12f); arcTo(2.5f, 2.5f, 0f, true, true, 6f, 14.5f); arcTo(2.5f, 2.5f, 0f, true, true, 3.5f, 12f); arcTo(2.5f, 2.5f, 0f, true, true, 8.5f, 12f)
    moveTo(20.5f, 5f); arcTo(2.5f, 2.5f, 0f, true, true, 18f, 7.5f); arcTo(2.5f, 2.5f, 0f, true, true, 15.5f, 5f); arcTo(2.5f, 2.5f, 0f, true, true, 20.5f, 5f)
    moveTo(20.5f, 19f); arcTo(2.5f, 2.5f, 0f, true, true, 18f, 21.5f); arcTo(2.5f, 2.5f, 0f, true, true, 15.5f, 19f); arcTo(2.5f, 2.5f, 0f, true, true, 20.5f, 19f)
    moveTo(8.2f, 10.8f); lineTo(15.8f, 6.2f)
    moveTo(8.2f, 13.2f); lineTo(15.8f, 17.8f)
}}

val IconLike: ImageVector get() = icon { sp {
    moveTo(12f, 20.5f)
    curveTo(12f, 20.5f, 3.5f, 15f, 3.5f, 9.5f)
    arcTo(4.2f, 4.2f, 0f, false, true, 12f, 7.8f)
    arcTo(4.2f, 4.2f, 0f, false, true, 20.5f, 9.5f)
    curveTo(20.5f, 15f, 12f, 20.5f, 12f, 20.5f)
}}

val IconStar: ImageVector get() = icon { sp {
    moveTo(12f, 3f); lineTo(14.7f, 8.6f); lineTo(21f, 9.5f)
    lineTo(16.5f, 13.8f); lineTo(17.6f, 20f); lineTo(12f, 17f)
    lineTo(6.4f, 20f); lineTo(7.5f, 13.8f); lineTo(3f, 9.5f)
    lineTo(9.3f, 8.6f); close()
}}

val IconChat: ImageVector get() = icon { sp {
    moveTo(21f, 14.5f)
    arcTo(2f, 2f, 0f, false, true, 19f, 16.5f)
    lineTo(7.5f, 16.5f); lineTo(3.5f, 20.5f); lineTo(3.5f, 5.5f)
    arcTo(2f, 2f, 0f, false, true, 5.5f, 3.5f)
    lineTo(19f, 3.5f)
    arcTo(2f, 2f, 0f, false, true, 21f, 5.5f)
    close()
}}

// ─── 功能 ───

val IconSettings: ImageVector get() = icon { sp {
    moveTo(14.7f, 12f); arcTo(2.7f, 2.7f, 0f, true, true, 12f, 14.7f); arcTo(2.7f, 2.7f, 0f, true, true, 9.3f, 12f); arcTo(2.7f, 2.7f, 0f, true, true, 14.7f, 12f)
    moveTo(12f, 2.5f); lineTo(12f, 4.5f)
    moveTo(12f, 19.5f); lineTo(12f, 21.5f)
    moveTo(2.5f, 12f); lineTo(4.5f, 12f)
    moveTo(19.5f, 12f); lineTo(21.5f, 12f)
    moveTo(5.3f, 5.3f); lineTo(6.7f, 6.7f)
    moveTo(17.3f, 17.3f); lineTo(18.7f, 18.7f)
    moveTo(5.3f, 18.7f); lineTo(6.7f, 17.3f)
    moveTo(17.3f, 6.7f); lineTo(18.7f, 5.3f)
}}

val IconHistory: ImageVector get() = icon { sp {
    moveTo(21.5f, 12f)
    arcTo(9.5f, 9.5f, 0f, true, true, 12f, 21.5f)
    arcTo(9.5f, 9.5f, 0f, true, true, 2.5f, 12f)
    arcTo(9.5f, 9.5f, 0f, true, true, 21.5f, 12f)
    moveTo(12f, 6.5f); lineTo(12f, 12f); lineTo(15.5f, 14f)
}}

val IconRank: ImageVector get() = icon { sp {
    moveTo(4f, 20f); lineTo(4f, 13f); lineTo(8f, 13f); lineTo(8f, 20f)
    moveTo(10f, 20f); lineTo(10f, 6f); lineTo(14f, 6f); lineTo(14f, 20f)
    moveTo(16f, 20f); lineTo(16f, 9.5f); lineTo(20f, 9.5f); lineTo(20f, 20f)
    moveTo(2.5f, 20f); lineTo(21.5f, 20f)
}}

val IconBack: ImageVector get() = icon { sp {
    moveTo(19f, 12f); lineTo(5f, 12f)
    moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
}}

val IconCancel: ImageVector get() = icon { sp {
    moveTo(17.5f, 6.5f); lineTo(6.5f, 17.5f)
    moveTo(6.5f, 6.5f); lineTo(17.5f, 17.5f)
}}

val IconMore: ImageVector get() = icon { fp {
    moveTo(12f, 5.5f); arcTo(1.2f, 1.2f, 0f, true, true, 12f, 5.51f)
    moveTo(12f, 12f); arcTo(1.2f, 1.2f, 0f, true, true, 12f, 12.01f)
    moveTo(12f, 18.5f); arcTo(1.2f, 1.2f, 0f, true, true, 12f, 18.51f)
}}

val IconHD: ImageVector get() = icon { sp {
    moveTo(4f, 8f); lineTo(4f, 16f)
    moveTo(4f, 12f); lineTo(8.5f, 12f)
    moveTo(8.5f, 8f); lineTo(8.5f, 16f)
    moveTo(12.5f, 8f); lineTo(12.5f, 16f)
    moveTo(12.5f, 8f); curveTo(16.5f, 8f, 19f, 10f, 19f, 12f)
    curveTo(19f, 14f, 16.5f, 16f, 12.5f, 16f)
}}

val IconNotice: ImageVector get() = icon { sp {
    moveTo(18f, 8.5f)
    arcTo(6f, 6f, 0f, false, false, 6f, 8.5f)
    lineTo(4.5f, 16.5f); lineTo(19.5f, 16.5f); close()
    moveTo(10f, 16.5f)
    arcTo(2f, 2f, 0f, false, false, 14f, 16.5f)
}}

val IconFloating: ImageVector get() = icon { sp {
    moveTo(3f, 7f); arcTo(2f, 2f, 0f, false, true, 5f, 5f)
    lineTo(19f, 5f); arcTo(2f, 2f, 0f, false, true, 21f, 7f)
    lineTo(21f, 17f); arcTo(2f, 2f, 0f, false, true, 19f, 19f)
    lineTo(5f, 19f); arcTo(2f, 2f, 0f, false, true, 3f, 17f); close()
    moveTo(13f, 12f); lineTo(19f, 12f); lineTo(19f, 17f); lineTo(13f, 17f); close()
}}

val IconBook: ImageVector get() = icon { sp {
    moveTo(4f, 19f); arcTo(2f, 2f, 0f, false, true, 6f, 17f)
    lineTo(20f, 17f); lineTo(20f, 3f); lineTo(6f, 3f)
    arcTo(2f, 2f, 0f, false, false, 4f, 5f)
    lineTo(4f, 19f); arcTo(2f, 2f, 0f, false, false, 6f, 21f); lineTo(20f, 21f)
    moveTo(9f, 7f); lineTo(15f, 7f)
    moveTo(9f, 11f); lineTo(13f, 11f)
}}

val IconOut: ImageVector get() = icon { sp {
    moveTo(9f, 21f); lineTo(5f, 21f); arcTo(2f, 2f, 0f, false, true, 3f, 19f)
    lineTo(3f, 5f); arcTo(2f, 2f, 0f, false, true, 5f, 3f); lineTo(9f, 3f)
    moveTo(16f, 17f); lineTo(21f, 12f); lineTo(16f, 7f)
    moveTo(21f, 12f); lineTo(9f, 12f)
}}

val IconPaint: ImageVector get() = icon { sp {
    moveTo(12f, 21.5f); arcTo(9.5f, 9.5f, 0f, false, true, 2.5f, 12f)
    arcTo(9.5f, 9.5f, 0f, false, true, 21.5f, 12f)
    curveTo(21.5f, 14f, 20.5f, 15.5f, 18.5f, 15.5f)
    curveTo(16.5f, 15.5f, 16f, 14f, 17f, 12.5f)
    curveTo(18f, 11f, 16f, 9.5f, 14f, 10.5f)
    moveTo(7.5f, 11f); arcTo(0.8f, 0.8f, 0f, true, true, 7.5f, 11.01f)
    moveTo(10.5f, 7.5f); arcTo(0.8f, 0.8f, 0f, true, true, 10.5f, 7.51f)
}}

val IconWelfare: ImageVector get() = icon { sp {
    moveTo(20f, 12f); lineTo(20f, 21f); lineTo(4f, 21f); lineTo(4f, 12f)
    moveTo(21.5f, 7f); lineTo(2.5f, 7f); lineTo(2.5f, 12f); lineTo(21.5f, 12f); close()
    moveTo(12f, 21f); lineTo(12f, 7f)
    moveTo(12f, 7f); curveTo(12f, 7f, 9.5f, 3.5f, 7.5f, 3.5f)
    arcTo(2f, 2f, 0f, false, false, 9.5f, 7f)
    moveTo(12f, 7f); curveTo(12f, 7f, 14.5f, 3.5f, 16.5f, 3.5f)
    arcTo(2f, 2f, 0f, false, true, 14.5f, 7f)
}}

val IconWeek: ImageVector get() = icon { sp {
    moveTo(4f, 5f); lineTo(20f, 5f); arcTo(1f, 1f, 0f, false, true, 21f, 6f)
    lineTo(21f, 19f); arcTo(1f, 1f, 0f, false, true, 20f, 20f)
    lineTo(4f, 20f); arcTo(1f, 1f, 0f, false, true, 3f, 19f)
    lineTo(3f, 6f); arcTo(1f, 1f, 0f, false, true, 4f, 5f)
    moveTo(16f, 3f); lineTo(16f, 7f)
    moveTo(8f, 3f); lineTo(8f, 7f)
    moveTo(3f, 10f); lineTo(21f, 10f)
}}

val IconSwap: ImageVector get() = icon { sp {
    moveTo(17f, 4f); lineTo(17f, 20f)
    moveTo(13.5f, 16.5f); lineTo(17f, 20f); lineTo(20.5f, 16.5f)
    moveTo(7f, 20f); lineTo(7f, 4f)
    moveTo(3.5f, 7.5f); lineTo(7f, 4f); lineTo(10.5f, 7.5f)
}}

val IconCast: ImageVector get() = icon { sp {
    moveTo(3f, 5.5f); lineTo(21f, 5.5f); lineTo(21f, 18.5f); lineTo(14f, 18.5f)
    moveTo(3f, 18.5f); arcTo(0.5f, 0.5f, 0f, true, true, 3f, 18.51f)
    moveTo(3f, 15f); arcTo(4f, 4f, 0f, false, true, 7f, 18.5f)
    moveTo(3f, 11.5f); arcTo(7.5f, 7.5f, 0f, false, true, 10.5f, 18.5f)
}}

val IconCastActive: ImageVector get() = icon { sp {
    moveTo(3f, 5.5f); lineTo(21f, 5.5f); lineTo(21f, 18.5f); lineTo(14f, 18.5f)
    moveTo(3f, 18.5f); arcTo(0.6f, 0.6f, 0f, true, true, 3f, 18.51f)
    moveTo(3f, 15f); arcTo(4f, 4f, 0f, false, true, 7f, 18.5f)
    moveTo(3f, 11.5f); arcTo(7.5f, 7.5f, 0f, false, true, 10.5f, 18.5f)
}}

val IconFeedback: ImageVector get() = icon { sp {
    moveTo(12f, 8f); lineTo(12f, 12.5f)
    moveTo(12f, 16f); lineTo(12f, 16.01f)
    moveTo(21.5f, 12f)
    arcTo(9.5f, 9.5f, 0f, true, true, 12f, 21.5f)
    arcTo(9.5f, 9.5f, 0f, true, true, 2.5f, 12f)
    arcTo(9.5f, 9.5f, 0f, true, true, 21.5f, 12f)
}}

val IconMail: ImageVector get() = icon { sp {
    moveTo(4f, 5.5f); lineTo(20f, 5.5f); arcTo(1.5f, 1.5f, 0f, false, true, 21.5f, 7f)
    lineTo(21.5f, 17f); arcTo(1.5f, 1.5f, 0f, false, true, 20f, 18.5f)
    lineTo(4f, 18.5f); arcTo(1.5f, 1.5f, 0f, false, true, 2.5f, 17f)
    lineTo(2.5f, 7f); arcTo(1.5f, 1.5f, 0f, false, true, 4f, 5.5f)
    moveTo(3f, 7f); lineTo(12f, 13f); lineTo(21f, 7f)
}}
