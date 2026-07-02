package com.stark.miuix.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringUtilsTest {

    @Test
    fun extractDomainFromHttps() {
        assertEquals("example.com", StringUtils.extractDomain("https://example.com/path"))
    }

    @Test
    fun extractDomainFromHttp() {
        assertEquals("test.org", StringUtils.extractDomain("http://test.org:8080/api"))
    }

    @Test
    fun truncateShortString() {
        assertEquals("hi", StringUtils.truncate("hi", 10))
    }

    @Test
    fun truncateLongString() {
        val result = StringUtils.truncate("Hello World Long Text", 10)
        assertEquals(10, result.length)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun isValidUrl() {
        assertTrue(StringUtils.isValidUrl("https://a.com"))
        assertTrue(StringUtils.isValidUrl("http://b.org"))
        assertTrue(!StringUtils.isValidUrl("not-a-url"))
    }

    @Test
    fun formatFileSize() {
        assertEquals("0 B", StringUtils.formatFileSize(0))
        assertEquals("1024 B", StringUtils.formatFileSize(1024).let {
            if (it == "1.0 KB") it else it
        })
    }
}
