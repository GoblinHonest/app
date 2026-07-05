/*
 * Copyright 2024 Stark Industries
 *
 * 自定义线条风格图标系统
 * 参考逆向 APK 图标集名称自行设计 SVG 路径
 * 统一规格：24x24 viewBox，1.5px 线宽，圆角线帽
 */

package com.stark.miuix.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** 统一线条风格 */
private val stroke = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val vbSize = 24f

/** 首页图标 — 房屋轮廓 */
val IconHome: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 屋顶三角
        moveTo(3f, 10f); lineTo(12f, 3f); lineTo(21f, 10f)
        // 左墙
        moveTo(5f, 10f); lineTo(5f, 20f)
        // 右墙
        moveTo(19f, 10f); lineTo(19f, 20f)
        // 地板
        moveTo(5f, 20f); lineTo(19f, 20f)
        // 门
        moveTo(10f, 20f); lineTo(10f, 15f); lineTo(14f, 15f); lineTo(14f, 20f)
    }.build()

/** 搜索图标 — 放大镜 */
val IconSearch: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 圆圈
        moveTo(17f, 11f)
        arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 17f)
        arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, 11f)
        arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 11f)
        // 手柄
        moveTo(15.5f, 15.5f); lineTo(20f, 20f)
    }.build()

/** 用户图标 — 人形轮廓 */
val IconUser: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 头部圆
        moveTo(16f, 8f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 12f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 8f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 8f)
        // 身体弧线
        moveTo(4f, 20f)
        curveTo(4f, 17f, 7.6f, 15f, 12f, 15f)
        curveTo(16.4f, 15f, 20f, 17f, 20f, 20f)
    }.build()

/** 播放图标 — 三角形 */
val IconPlay: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(8f, 5f); lineTo(19f, 12f); lineTo(8f, 19f); close()
    }.build()

/** 暂停图标 — 双竖线 */
val IconPause: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round
    ) {
        moveTo(9f, 5f); lineTo(9f, 19f)
        moveTo(15f, 5f); lineTo(15f, 19f)
    }.build()

/** 下一集图标 */
val IconNext: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(6f, 6f); lineTo(14f, 12f); lineTo(6f, 18f); close()
        moveTo(17f, 6f); lineTo(17f, 18f)
    }.build()

/** 全屏图标 */
val IconFullscreen: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round
    ) {
        // 左上角
        moveTo(4f, 9f); lineTo(4f, 4f); lineTo(9f, 4f)
        // 右上角
        moveTo(15f, 4f); lineTo(20f, 4f); lineTo(20f, 9f)
        // 右下角
        moveTo(20f, 15f); lineTo(20f, 20f); lineTo(15f, 20f)
        // 左下角
        moveTo(9f, 20f); lineTo(4f, 20f); lineTo(4f, 15f)
    }.build()

/** 锁定图标 */
val IconLock: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 锁身
        moveTo(6f, 11f); lineTo(6f, 20f); lineTo(18f, 20f); lineTo(18f, 11f); close()
        // 锁弓
        moveTo(8f, 11f); lineTo(8f, 7f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 7f)
        lineTo(16f, 11f)
        // 锁眼
        moveTo(12f, 15f); lineTo(12f, 16f)
    }.build()

/** 弹幕图标 */
val IconDanmaku: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round
    ) {
        // 3条滚动线
        moveTo(4f, 8f); lineTo(20f, 8f)
        moveTo(4f, 12f); lineTo(16f, 12f)
        moveTo(4f, 16f); lineTo(18f, 16f)
    }.build()

/** 下载图标 */
val IconDownload: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 4f); lineTo(12f, 15f)
        moveTo(8f, 11f); lineTo(12f, 15f); lineTo(16f, 11f)
        moveTo(4f, 18f); lineTo(4f, 20f); lineTo(20f, 20f); lineTo(20f, 18f)
    }.build()

/** 分享图标 */
val IconShare: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 三个点
        moveTo(6f, 12f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 14f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 14f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 12f)
        moveTo(18f, 5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 7f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 7f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 5f)
        moveTo(18f, 19f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 21f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 21f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 19f)
        // 连线
        moveTo(7.5f, 13f); lineTo(15.5f, 7f)
        moveTo(7.5f, 13f); lineTo(15.5f, 19f)
    }.build()

/** 点赞图标 — 心形 */
val IconLike: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 21f)
        curveTo(12f, 21f, 3f, 15f, 3f, 9f)
        arcTo(4.5f, 4.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 7.5f)
        arcTo(4.5f, 4.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 9f)
        curveTo(21f, 15f, 12f, 21f, 12f, 21f)
    }.build()

/** 收藏图标 — 五角星 */
val IconStar: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 2f); lineTo(15.09f, 8.26f); lineTo(22f, 9.27f)
        lineTo(17f, 14.14f); lineTo(18.18f, 21.02f); lineTo(12f, 17.77f)
        lineTo(5.82f, 21.02f); lineTo(7f, 14.14f); lineTo(2f, 9.27f)
        lineTo(8.91f, 8.26f); close()
    }.build()

/** 评论图标 — 气泡 */
val IconChat: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(21f, 15f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 17f)
        lineTo(7f, 17f); lineTo(3f, 21f); lineTo(3f, 5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, 3f)
        lineTo(19f, 3f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 5f)
        close()
    }.build()

/** 设置图标 — 齿轮 */
val IconSettings: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round
    ) {
        // 中心圆
        moveTo(15f, 12f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 15f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9f, 12f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 12f)
        // 外圈锯齿（简化为8个点）
        moveTo(12f, 2f); lineTo(12f, 4f)
        moveTo(12f, 20f); lineTo(12f, 22f)
        moveTo(2f, 12f); lineTo(4f, 12f)
        moveTo(20f, 12f); lineTo(22f, 12f)
        moveTo(4.93f, 4.93f); lineTo(6.34f, 6.34f)
        moveTo(17.66f, 17.66f); lineTo(19.07f, 19.07f)
        moveTo(4.93f, 19.07f); lineTo(6.34f, 17.66f)
        moveTo(17.66f, 6.34f); lineTo(19.07f, 4.93f)
    }.build()

/** 历史记录图标 — 时钟 */
val IconHistory: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round
    ) {
        // 圆
        moveTo(22f, 12f)
        arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 22f)
        arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 12f)
        arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 12f)
        // 指针
        moveTo(12f, 6f); lineTo(12f, 12f); lineTo(16f, 14f)
    }.build()

/** 排行榜图标 */
val IconRank: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 三根柱子
        moveTo(4f, 20f); lineTo(4f, 12f); lineTo(8f, 12f); lineTo(8f, 20f)
        moveTo(10f, 20f); lineTo(10f, 6f); lineTo(14f, 6f); lineTo(14f, 20f)
        moveTo(16f, 20f); lineTo(16f, 9f); lineTo(20f, 9f); lineTo(20f, 20f)
        // 基线
        moveTo(2f, 20f); lineTo(22f, 20f)
    }.build()

/** 返回图标 — 左箭头 */
val IconBack: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(19f, 12f); lineTo(5f, 12f)
        moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
    }.build()

/** 通知图标 — 铃铛 */
val IconNotice: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(18f, 8f)
        arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 8f)
        lineTo(4f, 17f); lineTo(20f, 17f); close()
        moveTo(10f, 17f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14f, 17f)
    }.build()

/** 悬浮播放图标 */
val IconFloating: ImageVector
    get() = ImageVector.Builder(
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = vbSize, viewportHeight = vbSize
    ).path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // 外框
        moveTo(3f, 7f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, 5f)
        lineTo(19f, 5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 7f)
        lineTo(21f, 17f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 19f)
        lineTo(5f, 19f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 17f)
        close()
        // 内嵌小窗
        moveTo(13f, 12f); lineTo(19f, 12f); lineTo(19f, 17f); lineTo(13f, 17f); close()
    }.build()
