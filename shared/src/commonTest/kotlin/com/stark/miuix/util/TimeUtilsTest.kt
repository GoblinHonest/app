package com.stark.miuix.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeUtilsTest {

    @Test
    fun justNow() {
        val now = currentTimeMillis()
        assertEquals("刚刚", TimeUtils.formatRelative(now - 10_000))
    }

    @Test
    fun minutesAgo() {
        val now = currentTimeMillis()
        val result = TimeUtils.formatRelative(now - 5 * 60_000)
        assertTrue(result.contains("5 分钟前"))
    }

    @Test
    fun hoursAgo() {
        val now = currentTimeMillis()
        val result = TimeUtils.formatRelative(now - 3 * 3_600_000)
        assertTrue(result.contains("3 小时前"))
    }

    @Test
    fun yesterday() {
        val now = currentTimeMillis()
        val result = TimeUtils.formatRelative(now - 30 * 3_600_000)
        assertEquals("昨天", result)
    }

    @Test
    fun daysAgo() {
        val now = currentTimeMillis()
        val result = TimeUtils.formatRelative(now - 5L * 86_400_000)
        assertTrue(result.contains("5 天前"))
    }

    @Test
    fun weeksAgo() {
        val now = currentTimeMillis()
        val result = TimeUtils.formatRelative(now - 14L * 86_400_000)
        assertTrue(result.contains("2 周前"))
    }

    @Test
    fun monthsAgo() {
        val now = currentTimeMillis()
        val result = TimeUtils.formatRelative(now - 60L * 86_400_000)
        assertTrue(result.contains("个月前"))
    }
}
